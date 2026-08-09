# NightFlow on AWS (EKS + ECR)

OpenTofu configuration that creates everything NightFlow needs to run on AWS:

| Resource | How |
|---|---|
| VPC, public/private subnets, NAT, routing | [`terraform-aws-modules/vpc/aws`](https://registry.terraform.io/modules/terraform-aws-modules/vpc/aws) `~> 5.19` |
| EKS control plane, managed node group, core add-ons | [`terraform-aws-modules/eks/aws`](https://registry.terraform.io/modules/terraform-aws-modules/eks/aws) `~> 20.31` |
| One ECR repository per service (11), scan-on-push, lifecycle policy | [`terraform-aws-modules/ecr/aws`](https://registry.terraform.io/modules/terraform-aws-modules/ecr/aws) `~> 2.3` |

Official modules are used on purpose: an EKS cluster written from raw resources
is several hundred lines of IAM, launch templates and add-on wiring that these
modules already get right.

The tooling is **OpenTofu** (`tofu`). The configuration itself is plain HCL and
works with `terraform` too, but the committed `.terraform.lock.hcl` pins
providers by their `registry.opentofu.org` addresses — delete it and let
`terraform init` regenerate the lock file before using Terraform.

## What is not here

- **No RDS / ElastiCache / DocumentDB / MSK.** The `k8s/overlays/prod` ConfigMap
  expects managed endpoints, but provisioning them is out of scope for this
  configuration - it stops at the cluster and the registries.
- **No DNS or certificates.** The Ingress in `k8s/` assumes an ingress-nginx
  controller and cert-manager are installed in the cluster; neither is created
  here.
- **No remote state backend configured.** The S3 backend is written but commented
  out in `versions.tf`, because the bucket has to exist first.

## Prerequisites

- OpenTofu >= 1.6 (or Terraform >= 1.6)
- AWS CLI v2, authenticated (`aws sts get-caller-identity` must work)
- `kubectl`
- An IAM principal allowed to create VPC, EKS, IAM and ECR resources

## Usage

```bash
cd deploy/aws

cp terraform.tfvars.example terraform.tfvars   # then edit region, sizes, CIDRs
tofu init
tofu plan
tofu apply

# Point kubectl at the new cluster
$(tofu output -raw configure_kubectl)
kubectl get nodes
```

Push images to the registries this created:

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=$(tofu output -raw region)
REGISTRY="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"

aws ecr get-login-password --region "$REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  docker build -t "$REGISTRY/nightflow/$svc:1.0.0" "../../$svc"
  docker push "$REGISTRY/nightflow/$svc:1.0.0"
done
```

Then deploy the manifests, pointing them at ECR instead of GHCR:

```bash
cd ../../k8s/overlays/prod
for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  kustomize edit set image "nightflow/$svc=$REGISTRY/nightflow/$svc:1.0.0"
done
kubectl apply -k .
```

Tear everything down:

```bash
cd deploy/aws
tofu destroy
```

> ECR repositories are created with `force_delete` in non-production
> environments, so `destroy` removes them even when they still contain images.

## Cost warning

An EKS control plane costs about **$0.10/hour** on its own, and this
configuration adds three `t3.large` nodes plus a NAT gateway. Running it for a
day is a few dollars; leaving it running for a month is not. `tofu destroy` when
you are done.

## Verification status

`tofu init -backend=false`, `tofu validate` and `tofu fmt -check` all pass
against the pinned module versions. **`tofu plan` and `tofu apply` have never
been run** - that requires real AWS credentials, which this repository does not
have. Treat the configuration as syntactically and semantically valid but not
as proven against the live API.
