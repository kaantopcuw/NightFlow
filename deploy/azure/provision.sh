#!/usr/bin/env bash
#
# Creates an AKS cluster and an ACR registry for NightFlow using the Azure CLI.
#
# This is a convenience wrapper around six `az` commands, NOT infrastructure as
# code: there is no state file, no plan, no drift detection. The AWS path
# (deploy/aws) is the one modelled properly. See README.md.
#
# Usage:  ./provision.sh
#         LOCATION=northeurope ACR_NAME=mynightflowacr ./provision.sh

set -euo pipefail

RESOURCE_GROUP="${RESOURCE_GROUP:-nightflow-rg}"
LOCATION="${LOCATION:-westeurope}"
# ACR names are globally unique and must be alphanumeric only.
ACR_NAME="${ACR_NAME:-nightflowacr}"
AKS_NAME="${AKS_NAME:-nightflow-aks}"
NODE_COUNT="${NODE_COUNT:-3}"
NODE_SIZE="${NODE_SIZE:-Standard_D2s_v5}"
K8S_VERSION="${K8S_VERSION:-1.31}"

command -v az >/dev/null || { echo "az CLI not found" >&2; exit 1; }

echo "==> Subscription: $(az account show --query name -o tsv)"

echo "==> Resource group ${RESOURCE_GROUP} (${LOCATION})"
az group create \
  --name "${RESOURCE_GROUP}" \
  --location "${LOCATION}" \
  --output none

echo "==> Container registry ${ACR_NAME}"
az acr create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${ACR_NAME}" \
  --sku Basic \
  --output none

echo "==> AKS cluster ${AKS_NAME} (${NODE_COUNT} x ${NODE_SIZE})"
az aks create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${AKS_NAME}" \
  --kubernetes-version "${K8S_VERSION}" \
  --node-count "${NODE_COUNT}" \
  --node-vm-size "${NODE_SIZE}" \
  --enable-managed-identity \
  --enable-cluster-autoscaler \
  --min-count 2 \
  --max-count 5 \
  --network-plugin azure \
  --generate-ssh-keys \
  --attach-acr "${ACR_NAME}" \
  --output none

echo "==> Writing kubeconfig"
az aks get-credentials \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${AKS_NAME}" \
  --overwrite-existing

cat <<EOF

Done.

  Registry : $(az acr show --name "${ACR_NAME}" --query loginServer -o tsv)
  Cluster  : ${AKS_NAME}

Next: build and push the images, then apply k8s/overlays/prod (see README.md).
Tear down with:  az group delete --name ${RESOURCE_GROUP} --yes --no-wait
EOF
