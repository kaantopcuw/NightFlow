# Kubernetes manifests

Kustomize layout:

```
k8s/
├── base/        11 Deployments + Services, the gateway Ingress,
│                the shared ConfigMap and Secret generators
├── infra/       dev-grade PostgreSQL, MongoDB, Redis, Kafka, Loki, Tempo
└── overlays/
    ├── dev/     base + infra, small resource requests, *.dev.local host
    └── prod/    base only, managed-datastore endpoints, replicas, TLS
```

`base` is not deployable on its own by design - it has no image registry and no
environment-specific endpoints. Always apply an overlay.

## Deploy

```bash
# Render without applying (what CI checks)
kubectl kustomize k8s/overlays/dev

# Apply
kubectl apply -k k8s/overlays/dev
kubectl -n nightflow get pods -w
```

Reach the gateway:

```bash
kubectl -n nightflow port-forward svc/gateway-service 8080:8080
# or add "127.0.0.1 nightflow.dev.local" to /etc/hosts and use the Ingress
```

## Using locally built images

The overlays point at `ghcr.io/kaantopcuw/nightflow/<service>`. For a local
cluster, build into the cluster's own daemon and override the images:

```bash
# minikube
eval "$(minikube docker-env)"
for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  docker build -t "nightflow/$svc:local" "$svc"
done

# kind
kind load docker-image nightflow/config-server:local   # ...and the rest

cd k8s/overlays/dev
for svc in config-server discovery-server gateway-service auth-service \
           venue-service event-catalog-service ticket-service \
           shopping-cart-service order-service notification-service \
           checkin-service; do
  kustomize edit set image "nightflow/$svc=nightflow/$svc:local"
done
kubectl apply -k .
```

## Design notes

**Start-up order.** There is none, on purpose. Kubernetes has no `depends_on`;
instead every service tolerates its dependencies being absent at boot
(`spring.config.import` is `optional:`) and the `startupProbe` gives each pod up
to five minutes to become ready. Pods that start before config-server restart
until it answers, which is the normal Kubernetes convergence model.

**Probes.** All three kinds, all pointing at `/actuator/health`:
`startupProbe` absorbs the slow JVM boot, `readinessProbe` keeps traffic away
until the context is up, `livenessProbe` restarts a wedged JVM. Liveness can be
short only because startup is handled separately.

**Service discovery.** Eureka still runs; it is how the gateway resolves
`lb://auth-service`. `EUREKA_PREFER_IP_ADDRESS=true` makes every instance
register its pod IP, which is routable cluster-wide, instead of its pod
hostname, which is not.

**Security context.** Non-root (uid 1001, matching the image), read-only root
filesystem with an `emptyDir` on `/tmp` for the JVM's scratch space, all
capabilities dropped, no privilege escalation, `RuntimeDefault` seccomp.

**Resources.** Memory requests and limits are set; **CPU has a request but no
limit** - a throttled JVM takes minutes to warm up and flaps its probes.

**Loki and Tempo are infrastructure, not extras.** The nine application
services (everything except config-server and discovery-server) log through a
Loki appender and export spans through an OTLP/gRPC exporter built in their own
`TracingConfig` class. A sink that is not reachable means a connection stack
trace every few seconds, so the dev overlay runs both. The prod overlay points
`LOKI_URL` and `NIGHTFLOW_TRACING_OTLP_GRPC_ENDPOINT` at an `observability`
namespace you are expected to provide.

**Secrets.** `base/kustomization.yaml` generates a Secret from literals. Those
are the repository's throwaway development values, present so that
`kubectl apply -k` produces something that actually starts. For a real cluster,
delete that generator and supply the Secret through External Secrets Operator,
Sealed Secrets, SOPS or a CSI secret store.

## Verification status

What was actually run:

- `kubectl kustomize` renders both overlays cleanly
- the rendered output passes `kubeconform -strict` against the Kubernetes 1.31
  schemas - dev: 42 resources, prod: 26 resources, 0 invalid
- `kubectl apply --dry-run=server` against a real API server (a throwaway `kind`
  cluster) accepted every object in both overlays: 42 and 26 resources, no errors

What was **not** run: the pods have never actually been scheduled. Server-side
dry-run proves the API server accepts the objects - it says nothing about
whether the JVMs fit in the memory limits, whether the probe timings are right,
or whether the images pull. Expect to iterate on those the first time you deploy
for real.
