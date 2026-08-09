# NightFlow on Azure (AKS + ACR)

> **This path is deliberately shallower than the AWS one.**
> `deploy/aws/` contains a complete, validated OpenTofu configuration (VPC, EKS,
> ECR). This directory contains **no infrastructure-as-code at all** - only a
> documented CLI walkthrough and one convenience script. Nothing here has been
> executed against a real Azure subscription. If you need Azure parity with AWS,
> the honest answer is that it still has to be written.

## What you get

- `provision.sh` - creates a resource group, an ACR and an AKS cluster with the
  Azure CLI, then attaches the registry to the cluster.
- The manifests in `k8s/` work unchanged on AKS; only the image registry and the
  ingress host differ.

## Prerequisites

- Azure CLI (`az`), logged in: `az login`
- A subscription selected: `az account set --subscription <id>`
- `kubectl`, `docker`

## Provision

```bash
cd deploy/azure
./provision.sh                       # uses the defaults below
LOCATION=northeurope ./provision.sh  # or override with environment variables
```

Defaults (all overridable via environment variables):

| Variable | Default | Meaning |
|---|---|---|
| `RESOURCE_GROUP` | `nightflow-rg` | Resource group name |
| `LOCATION` | `westeurope` | Azure region |
| `ACR_NAME` | `nightflowacr` | Registry name (must be globally unique - change it) |
| `AKS_NAME` | `nightflow-aks` | Cluster name |
| `NODE_COUNT` | `3` | Nodes in the default pool |
| `NODE_SIZE` | `Standard_D2s_v5` | VM size |
| `K8S_VERSION` | `1.31` | Kubernetes version |

## Build and push images

```bash
ACR_LOGIN_SERVER=$(az acr show --name "$ACR_NAME" --query loginServer -o tsv)
az acr login --name "$ACR_NAME"

for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  docker build -t "$ACR_LOGIN_SERVER/nightflow/$svc:1.0.0" "../../$svc"
  docker push "$ACR_LOGIN_SERVER/nightflow/$svc:1.0.0"
done
```

## Deploy

```bash
az aks get-credentials --resource-group "$RESOURCE_GROUP" --name "$AKS_NAME"

# ingress-nginx, which the Ingress manifest expects
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

cd ../../k8s/overlays/prod
for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  kustomize edit set image "nightflow/$svc=$ACR_LOGIN_SERVER/nightflow/$svc:1.0.0"
done
kubectl apply -k .
kubectl -n nightflow get pods -w
```

## Managed backing services

The prod overlay expects managed datastores. On Azure the equivalents are:

| NightFlow needs | Azure service |
|---|---|
| PostgreSQL | Azure Database for PostgreSQL Flexible Server |
| MongoDB | Azure Cosmos DB for MongoDB |
| Redis | Azure Cache for Redis |
| Kafka | Azure Event Hubs (Kafka protocol endpoint) |

Set their endpoints in the `nightflow-config` ConfigMap of the prod overlay.

## Tear down

```bash
az group delete --name "$RESOURCE_GROUP" --yes --no-wait
```
