# NightFlow on Google Cloud (GKE + Artifact Registry)

> **This path is deliberately shallower than the AWS one.**
> `deploy/aws/` contains a complete, validated OpenTofu configuration (VPC, EKS,
> ECR). This directory contains **no infrastructure-as-code at all** - only a
> documented CLI walkthrough and one convenience script. Nothing here has been
> executed against a real GCP project. If you need GCP parity with AWS, the
> honest answer is that it still has to be written.

## What you get

- `provision.sh` - enables the required APIs, creates an Artifact Registry
  repository and a GKE cluster with `gcloud`.
- The manifests in `k8s/` work unchanged on GKE; only the image registry and the
  ingress host differ.

## Prerequisites

- `gcloud` CLI, authenticated: `gcloud auth login`
- A project selected: `gcloud config set project <project-id>`
- Billing enabled on that project
- `kubectl`, `docker`

## Provision

```bash
cd deploy/gcp
PROJECT_ID=my-project ./provision.sh
PROJECT_ID=my-project REGION=europe-west4 ./provision.sh
```

Defaults (all overridable via environment variables):

| Variable | Default | Meaning |
|---|---|---|
| `PROJECT_ID` | current `gcloud` project | Target project |
| `REGION` | `europe-west1` | Region for the registry and the cluster |
| `CLUSTER_NAME` | `nightflow-gke` | GKE cluster name |
| `REPO_NAME` | `nightflow` | Artifact Registry repository |
| `NODE_COUNT` | `3` | Nodes per zone in the default pool |
| `MACHINE_TYPE` | `e2-standard-2` | Node machine type |

The script creates a **regional Standard cluster** (not Autopilot) with node
auto-scaling between 1 and 3 nodes per zone; adjust to taste before running it
anywhere that matters.

## Build and push images

```bash
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}"
gcloud auth configure-docker "${REGION}-docker.pkg.dev"

for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  docker build -t "$REGISTRY/$svc:1.0.0" "../../$svc"
  docker push "$REGISTRY/$svc:1.0.0"
done
```

## Deploy

```bash
gcloud container clusters get-credentials "$CLUSTER_NAME" --region "$REGION"

# ingress-nginx, which the Ingress manifest expects. (GKE also ships its own
# Ingress controller - if you use that instead, change ingressClassName in
# k8s/base/ingress.yaml to "gce".)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

cd ../../k8s/overlays/prod
for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  kustomize edit set image "nightflow/$svc=$REGISTRY/$svc:1.0.0"
done
kubectl apply -k .
kubectl -n nightflow get pods -w
```

## Managed backing services

| NightFlow needs | Google Cloud service |
|---|---|
| PostgreSQL | Cloud SQL for PostgreSQL |
| MongoDB | MongoDB Atlas on GCP (no first-party equivalent) |
| Redis | Memorystore for Redis |
| Kafka | Managed Service for Apache Kafka, or Pub/Sub with a code change |

Set their endpoints in the `nightflow-config` ConfigMap of the prod overlay.

## Tear down

```bash
gcloud container clusters delete "$CLUSTER_NAME" --region "$REGION" --quiet
gcloud artifacts repositories delete "$REPO_NAME" --location "$REGION" --quiet
```
