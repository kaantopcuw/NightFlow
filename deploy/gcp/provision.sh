#!/usr/bin/env bash
#
# Creates a GKE cluster and an Artifact Registry repository for NightFlow.
#
# This is a convenience wrapper around a handful of `gcloud` commands, NOT
# infrastructure as code: there is no state file, no plan, no drift detection.
# The AWS path (deploy/aws) is the one modelled properly. See README.md.
#
# Usage:  PROJECT_ID=my-project ./provision.sh
#         PROJECT_ID=my-project REGION=europe-west4 ./provision.sh

set -euo pipefail

command -v gcloud >/dev/null || { echo "gcloud CLI not found" >&2; exit 1; }

PROJECT_ID="${PROJECT_ID:-$(gcloud config get-value project 2>/dev/null)}"
REGION="${REGION:-europe-west1}"
CLUSTER_NAME="${CLUSTER_NAME:-nightflow-gke}"
REPO_NAME="${REPO_NAME:-nightflow}"
NODE_COUNT="${NODE_COUNT:-3}"
MACHINE_TYPE="${MACHINE_TYPE:-e2-standard-2}"

if [[ -z "${PROJECT_ID}" || "${PROJECT_ID}" == "(unset)" ]]; then
  echo "PROJECT_ID is not set and no default project is configured." >&2
  exit 1
fi

echo "==> Project: ${PROJECT_ID}, region: ${REGION}"

echo "==> Enabling APIs"
gcloud services enable \
  container.googleapis.com \
  artifactregistry.googleapis.com \
  --project "${PROJECT_ID}"

echo "==> Artifact Registry repository ${REPO_NAME}"
gcloud artifacts repositories create "${REPO_NAME}" \
  --repository-format=docker \
  --location="${REGION}" \
  --description="NightFlow service images" \
  --project "${PROJECT_ID}" \
  || echo "    (already exists, continuing)"

echo "==> GKE cluster ${CLUSTER_NAME} (${NODE_COUNT} nodes x ${MACHINE_TYPE})"
gcloud container clusters create "${CLUSTER_NAME}" \
  --region "${REGION}" \
  --num-nodes "${NODE_COUNT}" \
  --machine-type "${MACHINE_TYPE}" \
  --enable-autoscaling --min-nodes 1 --max-nodes 3 \
  --enable-ip-alias \
  --release-channel regular \
  --project "${PROJECT_ID}"

echo "==> Writing kubeconfig"
gcloud container clusters get-credentials "${CLUSTER_NAME}" \
  --region "${REGION}" \
  --project "${PROJECT_ID}"

cat <<EOF

Done.

  Registry : ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}
  Cluster  : ${CLUSTER_NAME} (${REGION})

Next: build and push the images, then apply k8s/overlays/prod (see README.md).
Tear down with:
  gcloud container clusters delete ${CLUSTER_NAME} --region ${REGION} --quiet
EOF
