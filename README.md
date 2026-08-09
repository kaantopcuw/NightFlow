<h1 align="center">🎫 NightFlow</h1>

<p align="center">
  <strong>A High-Performance Event Ticketing & Management Platform</strong>
</p>

<p align="center">
  <a href="#-features"><img src="https://img.shields.io/badge/Features-Explore-blue?style=for-the-badge" alt="Features"></a>
  <a href="#-quick-start"><img src="https://img.shields.io/badge/Quick%20Start-Get%20Started-green?style=for-the-badge" alt="Quick Start"></a>
  <a href="#-architecture"><img src="https://img.shields.io/badge/Architecture-Learn%20More-purple?style=for-the-badge" alt="Architecture"></a>
  <a href="#-contributing"><img src="https://img.shields.io/badge/Contributing-Welcome-orange?style=for-the-badge" alt="Contributing"></a>
</p>

<p align="center">
  <a href="https://github.com/kaantopcuw/NightFlow/actions/workflows/ci.yml"><img src="https://github.com/kaantopcuw/NightFlow/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <a href="https://github.com/kaantopcuw/NightFlow/actions/workflows/docker.yml"><img src="https://github.com/kaantopcuw/NightFlow/actions/workflows/docker.yml/badge.svg" alt="Container images"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.1-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.0.1">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.1.0-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=flat-square" alt="Status">
</p>

---

## 🌟 What is NightFlow?

**NightFlow** is a production-ready, cloud-native microservices platform designed to handle high-concurrency event ticketing, venue management, and real-time order processing. Think of it as the backbone for concerts, festivals, sports events, and more – built to handle thousands of simultaneous ticket purchases without breaking a sweat.

This project showcases modern backend engineering practices including event-driven architecture, distributed caching, a compensating saga for the payment flow (scope spelled out under [Saga scope](#-saga-scope-what-is-and-is-not-compensated)), and comprehensive E2E testing. Whether you're learning microservices or building your own ticketing system, NightFlow provides a solid foundation.

### ✨ Key Highlights

- 🚀 **Cutting-Edge Stack** – Built with Java 25 and Spring Boot 4.0.1, leveraging the latest features
- 📊 **Event-Driven Design** – Apache Kafka powers asynchronous communication between services
- ⚡ **High Performance** – Redis caching for sub-10ms check-in response times
- 🔒 **Secure by Default** – JWT authentication with Spring Security
- 📝 **API Documentation** – Interactive Swagger/OpenAPI docs for every service
- 🧪 **Thoroughly Tested** – Comprehensive E2E test suite with REST Assured

---

## 🏗️ Architecture

NightFlow follows a distributed microservices architecture pattern with centralized configuration and service discovery.

```
                                    ┌─────────────────────┐
                                    │   Config Server     │
                                    │      (8888)         │
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │  Discovery Server   │
                                    │   Eureka (8761)     │
                                    └──────────┬──────────┘
                                               │
                        ┌──────────────────────┼──────────────────────┐
                        │                      │                      │
             ┌──────────▼──────────┐           │          ┌──────────▼──────────┐
             │    API Gateway      │◄──────────┘          │   Message Broker    │
             │      (8080)         │                      │   Apache Kafka      │
             └──────────┬──────────┘                      └─────────┬───────────┘
                        │                                           │
    ┌───────────────────┼───────────────────┬───────────────────────┼───────────────────────┐
    │                   │                   │                       │                       │
┌───▼───┐  ┌───────────▼───────┐  ┌───────▼───────┐  ┌─────────────▼─────────────┐ ┌────────▼────────┐
│ Auth  │  │  Event Catalog    │  │    Ticket     │  │      Order Service        │ │ Check-in Service│
│(8090) │  │     (8092)        │  │    (8093)     │  │        (8095)             │ │    (8097)       │
│       │  │                   │  │               │  │                           │ │                 │
│ JWT   │  │ MongoDB + Redis   │  │  PostgreSQL   │  │ Pay saga + Kafka Producer │ │ Redis + QR Scan │
└───┬───┘  └───────────────────┘  └───────┬───────┘  └─────────────┬─────────────┘ └─────────────────┘
    │                                     │                        │               
    │      ┌───────────┐    ┌─────────────▼───────┐    ┌──────────▼──────────┐     
    │      │   Venue   │    │   Shopping Cart     │    │  Notification       │    
    └──────►  (8091)   │    │      (8094)         │    │     (8096)          │    
           │           │    │                     │    │                     │    
           │PostgreSQL │    │  Redis + Feign      │    │ Kafka Consumer+Mail │    
           └───────────┘    └─────────────────────┘    └─────────────────────┘    
                                                                             
```

### 📡 Data Flow

```
User → Gateway → Auth → Event Catalog → Ticket → Cart → Order → Kafka → Notification
                                           │                              │
                                           └──── Check-in (Event Day) ────┘
```

---

## 📦 Microservices

Microservices architecture is used in this project. Each service has its own responsibility and communicates with other services using **REST API** and **Kafka**.

<table>
<thead>
<tr>
<th align="left">Service</th>
<th align="left">Description</th>
<th align="center">Port</th>
<th align="left">Tech Stack</th>
</tr>
</thead>
<tbody>

<tr>
<td><strong>🔧 Config Server</strong></td>
<td>Centralized configuration management for all services</td>
<td align="center"><code>8888</code></td>
<td>Spring Cloud Config</td>
</tr>

<tr>
<td><strong>🔍 Discovery Server</strong></td>
<td>Service registry and discovery with health monitoring</td>
<td align="center"><code>8761</code></td>
<td>Netflix Eureka Server</td>
</tr>

<tr>
<td><strong>🚪 API Gateway</strong></td>
<td>Single entry point with routing, load balancing, and rate limiting</td>
<td align="center"><code>8080</code></td>
<td>Spring Cloud Gateway (WebFlux/Netty)</td>
</tr>

<tr>
<td><strong>🔐 Auth Service</strong></td>
<td>JWT-based authentication, user registration, and authorization</td>
<td align="center"><code>8090</code></td>
<td>Spring Security, JJWT 0.13.0, PostgreSQL</td>
</tr>

<tr>
<td><strong>🏟️ Venue Service</strong></td>
<td>Venue management, seat layouts, and organizer profiles</td>
<td align="center"><code>8091</code></td>
<td>Spring Data JPA, PostgreSQL</td>
</tr>

<tr>
<td><strong>📅 Event Catalog</strong></td>
<td>Event listings, search, filtering, and metadata management</td>
<td align="center"><code>8092</code></td>
<td>MongoDB, Redis Cache</td>
</tr>

<tr>
<td><strong>🎟️ Ticket Service</strong></td>
<td>Ticket inventory, reservations, and concurrency control</td>
<td align="center"><code>8093</code></td>
<td>PostgreSQL (Pessimistic Locking)</td>
</tr>

<tr>
<td><strong>🛒 Shopping Cart</strong></td>
<td>High-speed cart management with automatic expiration</td>
<td align="center"><code>8094</code></td>
<td>Redis (15-min TTL), OpenFeign</td>
</tr>

<tr>
<td><strong>📋 Order Service</strong></td>
<td>Order processing; paying an order is a compensating saga across order- and ticket-service</td>
<td align="center"><code>8095</code></td>
<td>Payment saga + compensation, Kafka Producer, PostgreSQL</td>
</tr>

<tr>
<td><strong>📧 Notification Service</strong></td>
<td>Async email/SMS notifications and ticket delivery</td>
<td align="center"><code>8096</code></td>
<td>Kafka Consumer, Spring Mail</td>
</tr>

<tr>
<td><strong>✅ Check-in Service</strong></td>
<td>Lightning-fast QR code validation for event entry (<10ms)</td>
<td align="center"><code>8097</code></td>
<td>Redis (Atomic Ops), OpenFeign</td>
</tr>

</tbody>
</table>

### 🔁 Saga scope: what is and is not compensated

"Saga Pattern" in this README means **one flow**: paying an order
(`POST /api/orders/{orderNumber}/pay`, `OrderService#payOrder`). It is a
hand-written orchestration inside that one method - there is no saga framework,
no state machine, no orchestrator service and no outbox.

The flow spans two databases, so there is no transaction that can cover it:

| # | Step | Compensated by |
|---|---|---|
| 1 | Capture the payment (**simulated** - no PSP is integrated) | a simulated refund, recorded on the order |
| 2 | `confirm-sale` each reserved item in `ticket-service` | `POST /tickets/release-sale?orderId=` - the tickets this order already had flipped to `SOLD` go back to free stock |
| 3 | Reservations that were never confirmed | `DELETE /tickets/reserve/{reservationId}` - a no-op for an item that *was* confirmed, because `confirm-sale` clears its session id |
| 4 | Publish `order-created` to Kafka | not needed: it is published **after** the sale is confirmed, so a failed order never triggers a confirmation e-mail |

`payOrder` is deliberately **not** `@Transactional`. It must not hold a database
transaction open across HTTP calls, and a surrounding transaction would roll the
failure record back together with the exception that reports it. Each state
change is its own short transaction in `OrderStateWriter`.

Two terminal statuses tell the two failure modes apart, and the API answers
`409 Conflict` with the status and the reason rather than a bare `500`:

- **`FAILED`** - the sale could not be confirmed and every compensating call
  succeeded. No ticket of this order is sold, the stock is back, the payment was
  reversed. The buyer starts a new order.
- **`COMPENSATION_FAILED`** - the compensation could not run either (typically
  `ticket-service` is unreachable, which is what broke the confirm in the first
  place). The order is still never `COMPLETED` and no ticket is handed out, but
  `ticket-service` may still be holding a reservation. `failureReason` records
  exactly which compensating call failed. The reservation is released by
  `ticket-service`'s existing 15-minute expiry sweeper; anything already
  confirmed would need releasing by hand.

Both statuses are stored on the order together with `paymentReference` and
`failureReason`, so a failed payment leaves a readable record instead of a log line.

**Not covered**, and deliberately so:

- every other cross-service call in the platform (cart, check-in, catalog) is
  unchanged - a failure there still leaves what it always left
- there is no retry, no idempotency key and no dead-letter path; a compensating
  call gets exactly one attempt
- the payment step is a placeholder. `capturePayment`/`refundPayment` move no
  money; they exist so the saga has the shape it would have with a real gateway
  and so the refund has a reference to quote
- Feign keeps its default 30-second connect timeout, so paying while
  `ticket-service` is down blocks the caller for ~30 s before answering `409`
- the order row is not locked for the duration of the saga, so two `pay` calls
  racing on the same `PENDING` order can still interleave. That race predates
  the compensation (the old code did not lock either) and closing it needs an
  `in-progress` status plus a `SELECT … FOR UPDATE`, which is a bigger change
  than this one
- if `order-service` dies mid-saga the order stays `PENDING`, which is the
  benign end of the failure space: no ticket is handed out and the reservation
  expires on its own, so the buyer can simply pay again

**Schema note.** `OrderStatus` is an `@Enumerated(STRING)` column, and Hibernate
generates a `CHECK` constraint from the enum values. `ddl-auto: update` creates
that constraint but never alters it, so a database created before `FAILED` and
`COMPENSATION_FAILED` existed will reject them. A fresh database is fine -
booting the service against an empty schema produces the constraint with all six
values, which was checked - but an existing one needs this once:

```sql
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
  CHECK (status IN ('PENDING','COMPLETED','CANCELLED','REFUNDED','FAILED','COMPENSATION_FAILED'));
```

## 🔭 Observability

NightFlow implements a complete **LGTM Stack** for monitoring:

| Tool | Port | Description |
|------|------|-------------|
| **Grafana** | `3000` | Visualization Dashboard (User: `admin` / Pass: `password`) |
| **Prometheus**| `9090` | Metrics Collection |
| **Loki** | `3100` | Centralized Logging |
| **Tempo** | `3200` | Distributed Tracing (OTLP) |

### 🕵️‍♂️ Distributed Tracing with Tempo

NightFlow uses **Grafana Tempo** to trace requests as they travel through the microservices. This allows you to visualize the full call chain, identify bottlenecks, and debug errors effectively.

#### How to functionality:
1.  **Make a Request**: Any request sent through the API Gateway (port 8080) will have a Trace ID generated automatically.
2.  **Get the Trace ID**: The Gateway returns a custom header `X-Trace-Id` in the response (e.g., `feb3b878bb36fc6fc05633557a717936`).
3.  **Visualize in Grafana**:
    *   Go to **Grafana** (http://localhost:3000).
    *   Navigate to **Explore** from the sidebar.
    *   Select **Tempo** as the data source.
    *   Paste the `X-Trace-Id` into the "Trace ID" field and run the query.
    *   You will see a Gantt chart showing the request's journey across all services (Gateway -> Auth -> Other Services).

👉 **[View Observability Guide](docs/observability.md)** for detailed usage instructions.

---

## 🛠️ Tech Stack

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Primary language with latest features |
| **Spring Boot** | 4.0.1 | Application framework |
| **Spring Cloud** | 2025.1.0 | Microservices infrastructure |

### Data & Messaging
| Technology | Purpose |
|------------|---------|
| **PostgreSQL** | Relational data (Auth, Venue, Ticket, Order) |
| **MongoDB** | Document store (Event Catalog) |
| **Redis** | Caching, Cart storage, Check-in data |
| **Apache Kafka** | Event-driven messaging |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| **Netflix Eureka** | Service discovery |
| **Spring Cloud Config** | Centralized configuration |
| **Spring Cloud Gateway** | API gateway (WebFlux) |
| **OpenFeign** | Declarative REST clients |

### Security & Docs
| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Security** | - | Authentication & Authorization |
| **JJWT** | 0.13.0 | JWT token handling |
| **SpringDoc OpenAPI** | 2.8.6 | Interactive API documentation |

### Testing
| Technology | Version | Purpose |
|------------|---------|---------|
| **REST Assured** | 5.4.0 | API testing |
| **JUnit 5** | 5.10.1 | Unit & Integration testing |
| **Awaitility** | 4.2.0 | Async testing utilities |

### DevOps
| Technology | Purpose |
|------------|---------|
| **Docker** | Multi-stage image per service (JRE runtime, non-root, health check) |
| **Docker Compose** | Full local stack: 11 services + PostgreSQL, MongoDB, Redis, Kafka |
| **Kubernetes + Kustomize** | Manifests with `dev` / `prod` overlays ([`k8s/`](k8s/)) |
| **GitHub Actions** | Build, test, manifest validation and image publishing to GHCR |
| **OpenTofu / Terraform** | EKS cluster and ECR registries on AWS ([`deploy/aws/`](deploy/aws/)) |
| **Maven** | Build & dependency management |

---

## 🚀 Quick Start

### Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **JDK 25** or later
- 🐋 **Docker** & **Docker Compose**
- 📦 **Maven 3.9+** (or use included `./mvnw` wrapper)

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/kaantopcuw/NightFlow.git
cd NightFlow
```

### 2️⃣ Start Everything with `manage.sh`

NightFlow includes a powerful CLI tool for managing all services:

```bash
# Start infrastructure (databases, Kafka, observability stack) + all services
./manage.sh start all

# Check status
./manage.sh status

# View logs
./manage.sh logs auth-service
```

This starts:
- 🐘 **PostgreSQL** (5432) - Auth, Venue, Ticket, Order databases
- 🍃 **MongoDB** (27017) - Event Catalog
- 🔴 **Redis** (6379) - Cart, Cache, Check-in
- 📨 **Kafka + Zookeeper** (9092, 2181) - Event messaging
- 📊 **Grafana** (3000), **Prometheus** (9090), **Loki** (3100), **Tempo** (3200) - Observability
- 🎯 **All 11 microservices**

### 3️⃣ Verify Installation

| Dashboard | URL |
|-----------|-----|
| **Eureka** (Service Registry) | http://localhost:8761 |
| **API Gateway** | http://localhost:8080 |
| **Grafana** (Monitoring) | http://localhost:3000 (admin/password) |

### 4️⃣ Run E2E Tests

```bash
cd e2e-tests
./mvnw verify
```

### `manage.sh` Commands

| Command | Description |
|---------|-------------|
| `./manage.sh start all` | Start infrastructure + all services |
| `./manage.sh start infra` | Start only Docker infrastructure |
| `./manage.sh start auth-service` | Start a specific service |
| `./manage.sh stop all` | Stop all services |
| `./manage.sh restart all` | Restart everything |
| `./manage.sh status` | Show status of all services |
| `./manage.sh logs <service>` | Tail logs for a service |

---

## 🚢 Deployment

Three ways to run NightFlow, in increasing order of effort: containers on your
laptop, Kubernetes, and a cloud cluster.

Configuration is environment-driven everywhere. The Config Server still owns the
property files, but every endpoint in them is a placeholder with a
local-development default:

```yaml
url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/nightflow_auth
```

So `./manage.sh start all` keeps working exactly as before, while a container
only has to set `POSTGRES_HOST=postgres`.

### 🐋 Local: Docker Compose

Everything - the four datastores and all 11 services - in one command:

```bash
cp .env.example .env          # optional; every value has a working default
docker compose up -d --build

docker compose ps             # wait until the services report "healthy"
curl http://localhost:8080/actuator/health
open http://localhost:8761    # Eureka: all instances registered
```

Loki and Tempo always run: the services write logs and spans to them from
application code, and an unreachable sink means stack traces in every container.
Grafana and Prometheus - the parts that let you *read* the telemetry - are
opt-in:

```bash
docker compose --profile observability up -d --build
open http://localhost:3000    # Grafana, admin/password
```

Tracing has no on/off switch: every service builds its own OpenTelemetry SDK in
a `TracingConfig` class and always exports over OTLP/gRPC to
`NIGHTFLOW_TRACING_OTLP_GRPC_ENDPOINT`. There is no Spring Boot tracing
auto-configuration on the classpath, so `management.tracing.*` would be inert -
those keys used to be set throughout the repository and were removed rather than
left to imply a switch that does not exist.

Stop and wipe the volumes:

```bash
docker compose down -v
```

Start-up order is enforced by health checks, not sleeps: config-server has to be
healthy before discovery-server starts, discovery-server before the gateway and
the business services, and each datastore before its dependents. The health
check is the service's own `/actuator/health`, so "healthy" means the Spring
context is up - not merely that the process launched.

Each service image is multi-stage: a Maven/JDK stage builds the fat jar, and a
JRE-only runtime stage runs it as an unprivileged user (uid 1001) with the
dependency layer cached separately from the sources.

```bash
docker build -t nightflow/auth-service:local auth-service   # build one by hand
```

### ☸️ Kubernetes

```bash
kubectl kustomize k8s/overlays/dev     # render
kubectl apply -k k8s/overlays/dev      # apply
kubectl -n nightflow get pods -w
```

| Overlay | Contents |
|---|---|
| `k8s/overlays/dev` | Application **plus** in-cluster PostgreSQL/MongoDB/Redis/Kafka/Loki/Tempo, small resource requests, `nightflow.dev.local` |
| `k8s/overlays/prod` | Application only - datastores are expected to be managed services - 2-3 replicas, larger requests, TLS ingress |

Every pod runs non-root with a read-only root filesystem, drops all
capabilities, declares memory requests and limits, and has all three probes
(`startupProbe` for the slow JVM boot, plus readiness and liveness) pointed at
`/actuator/health`.

See [`k8s/README.md`](k8s/README.md) for the layout, the design decisions, and
how to run it against locally built images.

### ☁️ Cloud

| Provider | What is here | Depth |
|---|---|---|
| **AWS** | [`deploy/aws/`](deploy/aws/) - OpenTofu: VPC, EKS cluster, 11 ECR repositories, using the official `terraform-aws-modules` | Full IaC, `tofu validate` clean |
| **Azure** | [`deploy/azure/`](deploy/azure/) - documented `az` walkthrough + `provision.sh` for AKS/ACR | **Scripts and docs only, no IaC** |
| **GCP** | [`deploy/gcp/`](deploy/gcp/) - documented `gcloud` walkthrough + `provision.sh` for GKE/Artifact Registry | **Scripts and docs only, no IaC** |

The Azure and GCP paths are intentionally shallower than AWS: a shell script
that calls the provider CLI is not infrastructure as code - it has no state, no
plan and no drift detection. Both READMEs say so explicitly.

```bash
cd deploy/aws
cp terraform.tfvars.example terraform.tfvars
tofu init && tofu plan && tofu apply
$(tofu output -raw configure_kubectl)
```

> **Cost warning:** an EKS control plane plus three `t3.large` nodes and a NAT
> gateway is not free. `tofu destroy` when you are finished.

### 🔄 CI/CD

| Workflow | Trigger | What it does |
|---|---|---|
| [`ci.yml`](.github/workflows/ci.yml) | PR + push to `main` | Matrix build of all 11 services, matrix test run against a PostgreSQL service container, and validation of `docker-compose.yml`, both Kustomize overlays and the OpenTofu configuration |
| [`docker.yml`](.github/workflows/docker.yml) | PR (build), push to `main` / `v*` tags (build + push) | Buildx image per service, published to `ghcr.io/<owner>/nightflow/<service>` |

The `@SpringBootTest` suites boot the real application context. In CI there is
no Config Server, so the workflow supplies the datastore properties as
environment variables - which is exactly the mechanism the containers use.

The `e2e-tests` module is **not** part of CI: it needs the whole platform
running. Run it locally against `docker compose up -d` instead.

### What has and has not been verified

Everything below was executed, not assumed:

- all 11 services package with Maven, and all 11 test suites pass with the environment the `test` job sets up
- all 11 images build; a container was run with `--read-only` and reports `uid=1001(nightflow)`
- the full Compose stack reaches 15/15 healthy in about 40 seconds, all 9 Eureka-registering
  services appear in the registry, all 8 gateway routes reach their service, Kafka's consumer
  group is assigned its partition, Tempo receives spans, and the aggregated container logs
  contain zero export failures, connection errors or start-up failures
- both Kustomize overlays render, pass `kubeconform -strict` against the Kubernetes 1.31
  schemas, and are accepted object-for-object by a real API server via
  `kubectl apply --dry-run=server` on a throwaway `kind` cluster (dev 42, prod 26 resources)
- `tofu init`, `tofu validate` and `tofu fmt -check` pass for `deploy/aws`
- the **purchase chain runs end to end** against the Compose stack: an
  `ORGANIZER` creates a ticket category, a `USER` adds it to the cart (which
  reserves stock in `ticket-service`), creates an order and pays it, `order-service`
  confirms the sale, and `GET /api/tickets/my-tickets` returns the `SOLD`
  tickets. `checkin-service` then preloads those tickets from `ticket-service`.
  All four cross-service Feign calls are exercised by that run.
- the `admin-panel` live suite (`npm run test:live`, 16 tests) passes against the
  same stack
- the **payment saga compensates under injected failure**, verified against the
  same live stack rather than in a unit test alone:
  - *partial failure* - a two-item order whose second reservation was consumed
    before payment. `confirm-sale` succeeded for item 1 and answered `404` for
    item 2; the API returned `409`, the order became `FAILED`, `my-tickets` did
    not grow, and the category returned to `sold=1 reserved=0` - the ticket that
    item 1 had already sold was released again.
  - *ticket-service stopped mid-saga* - `docker compose stop ticket-service`
    between order creation and payment. The API returned `409`, the order became
    `COMPENSATION_FAILED` (the compensating calls could not reach the service
    either) and `failureReason` names each one. No ticket was handed out. After
    `docker compose start ticket-service` the same category could be bought
    again end to end. The reservation the failed compensation stranded is
    released by `ticket-service`'s 15-minute expiry sweeper, measured on its
    own: a reservation taken at `17:58:57Z` was still held at `18:13:05Z` and
    gone at `18:14:05Z` - the 15 minutes plus one 60-second scheduler tick.
  - in both cases the order never reached `COMPLETED`, which is what the old
    `catch { log }` did, and `notification-service` logged an
    `order created event` for every `COMPLETED` order and for none of the
    failed ones - the Kafka publish really is on the success path only.
  - a `FAILED` order answers `409` when it is paid again, so a compensated
    order cannot be re-driven into `COMPLETED`.

### Defects found and closed

Three defects were found by exercising the platform against a live stack, and
all three are fixed. They are listed here rather than quietly removed, because
"we found this by running it" is the point.

| # | Defect | Fix | How it was re-verified |
|---|---|---|---|
| 1 | `POST /api/auth/register` copied the caller's `role` onto the new user unchecked. Registering with `"role": "SYSTEM"` yielded a token that `HeaderAuthFilter` turned into `ROLE_SYSTEM`, and the internal-only `GET /api/tickets/event/{id}/all` answered `200`. | `AuthenticationService` now matches the requested role against an allow-list of self-service roles (`USER`, `ORGANIZER`) and rejects anything else with `400`. | `"SYSTEM"`, `"system"`, `"ROLE_SYSTEM"` and `"ADMIN"` all return `400`; `"ORGANIZER"` still registers and can still create events; the internal endpoint answers `403` for a normal account. |
| 2 | Four Feign clients hard-coded `http://localhost:<port>`, which inside Docker is each container's own loopback. Creating a ticket category returned `500 Connection refused executing GET http://localhost:8092/events/…`, so no ticket could ever be sold. | All four now declare the Eureka service id as `@FeignClient(name = …)` and no `url`, which is what makes Spring Cloud resolve them through `lb://<service-id>`. | The full purchase chain above, plus `checkin-service` preloading the resulting tickets. |
| 3 | `AccessDeniedException` in `ticket-service` was swallowed by a catch-all `@ExceptionHandler(Exception.class)` and returned as `500`, so a deliberate deny was indistinguishable from a server fault. | An `AccessDeniedException` handler mapping to `403` was added ahead of the catch-all. | The internal endpoint now answers `403`, not `500`, for a non-`SYSTEM` caller. |

### Known limitations

Honest gaps that remain after those fixes:

- **The role allow-list is not retroactive.** It governs new registrations only.
  An account that was already stored with a privileged role keeps it, and there
  is no admin API or migration to change a role - this was confirmed live
  against an account created before the fix.
- **`ADMIN`, `GATEKEEPER` and `SYSTEM` have no provisioning path.** `SYSTEM` is
  synthesised by each service's `HeaderAuthFilter` for service-to-service calls
  and is not meant to belong to a human, but `GATEKEEPER` is: the two
  `POST /api/checkin/validate` and `GET /api/checkin/ticket/{code}` endpoints are
  `hasRole('GATEKEEPER')` and are therefore unreachable through the public API
  until an operator role is seeded out of band.
- **The same catch-all handler still masks `403` as `500`** in
  `event-catalog-service`, `shopping-cart-service` and `venue-service`. Only
  `ticket-service` was fixed, because only its behaviour was being asserted.
- **Compensation covers the payment saga only.** `OrderService#payOrder` rolls
  itself back (see [Saga scope](#-saga-scope-what-is-and-is-not-compensated)),
  but nothing else in the platform does. There is no saga framework, no
  orchestrator, no outbox and no retry: every other cross-service call still
  fails the way it always did.
- **Adding an `OrderStatus` value is a manual schema migration.** Hibernate
  generates a `CHECK` constraint from the enum, and `ddl-auto: update` never
  alters an existing one, so a database created before `FAILED` /
  `COMPENSATION_FAILED` existed rejects them with
  `orders_status_check`. There is no Flyway/Liquibase in this project; on an
  existing database run the statement in
  [Saga scope](#-saga-scope-what-is-and-is-not-compensated) once. A fresh
  database gets the right constraint from Hibernate at first start.
- **Running `ticket-service`'s test suite against a live stack deletes every
  held reservation.** Its `@SpringBootTest` boots the real application -
  `@EnableScheduling` included - against whatever database the config server
  points at, which on a developer machine is the Compose stack's own. `Ticket`
  timestamps are naive `LocalDateTime`: the containers run UTC and the host
  usually does not, so `now().minusMinutes(15)` evaluated on the host is hours
  ahead of every stored value and the expiry sweeper treats seconds-old
  reservations as expired. Measured: a reservation created at `17:58:16Z` was
  gone 23 seconds later, during `mvn -B test`. Carts survive (they live in
  Redis), their reservations do not.
- **`e2e-tests` is not part of CI** and was not run for this change; the
  verification above was done with direct HTTP calls plus the panel's live suite.

Not verified, for lack of the necessary accounts:

- the Kubernetes pods have never actually been **scheduled**; server-side dry-run
  proves the manifests are accepted, not that the workloads run
- `tofu plan` / `tofu apply` have never been run against AWS
- the Azure and GCP scripts have never been executed
- the GitHub Actions workflows have not yet run on GitHub

---

## 🧪 Testing

NightFlow includes a comprehensive E2E test suite that validates the complete user journey:

```
┌─────────────────────────────────────────────────────────────────┐
│                    E2E Test Flow                                │
├─────────────────────────────────────────────────────────────────┤
│  1. User Registration & Login       → JWT Token                 │
│  2. Browse Events & Tickets        → Event/Ticket Data          │
│  3. Add to Cart                    → Reserved Seats             │
│  4. Checkout & Order Creation      → Order Confirmation         │
│  5. Ticket Check-in (QR)           → Entry Validation           │
└─────────────────────────────────────────────────────────────────┘
```

### Run E2E Tests

```bash
cd e2e-tests
./mvnw verify
```

### Test Technologies
- **REST Assured 5.4.0** for fluent API testing
- **JUnit 5** for test organization
- **Awaitility** for async operation verification

---

## 📚 API Documentation

Each service exposes interactive Swagger documentation:

| Service | Swagger URL |
|---------|-------------|
| Auth Service | http://localhost:8090/swagger-ui.html |
| Venue Service | http://localhost:8091/swagger-ui.html |
| Event Catalog | http://localhost:8092/swagger-ui.html |
| Ticket Service | http://localhost:8093/swagger-ui.html |
| Shopping Cart | http://localhost:8094/swagger-ui.html |
| Order Service | http://localhost:8095/swagger-ui.html |
| Notification Service | http://localhost:8096/swagger-ui.html |
| Check-in Service | http://localhost:8097/swagger-ui.html |

---

## 📁 Project Structure

```
NightFlow/
├── 📂 config-server/         # Centralized configuration
│   ├── docker-compose.yml    # Infrastructure containers
│   └── config-repo/          # Service configurations
├── 📂 discovery-server/      # Eureka service registry
├── 📂 gateway-service/       # API Gateway (WebFlux)
├── 📂 auth-service/          # Authentication & JWT
├── 📂 venue-service/         # Venue management
├── 📂 event-catalog-service/ # Event listings (MongoDB)
├── 📂 ticket-service/        # Inventory management
├── 📂 shopping-cart-service/ # Cart operations (Redis)
├── 📂 order-service/         # Order processing (payment saga + compensation)
├── 📂 notification-service/  # Email/SMS (Kafka Consumer)
├── 📂 checkin-service/       # QR validation (Redis)
├── 📂 admin-panel/           # React + TypeScript operator panel (Vite, MSW)
├── 📂 e2e-tests/             # End-to-end test suite
├── 📂 docs/                  # Documentation & diagrams
│
├── 🐋 docker-compose.yml     # Full local stack (infra + 11 services)
├── 📄 .env.example           # Compose configuration template
├── 📂 docker/                # Compose-only assets (Postgres init, Prometheus)
├── 📂 k8s/                   # Kustomize: base + infra + dev/prod overlays
├── 📂 deploy/                # Cloud provisioning
│   ├── 📂 aws/               #   OpenTofu: VPC + EKS + ECR
│   ├── 📂 azure/             #   AKS/ACR walkthrough + script
│   └── 📂 gcp/               #   GKE/Artifact Registry walkthrough + script
└── 📂 .github/workflows/     # CI (build/test/manifests) and image publishing
```

Every service directory also contains its own `Dockerfile` and `.dockerignore`.

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and development process.

---

## � License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring Team](https://spring.io/) for the amazing framework
- [Netflix OSS](https://netflix.github.io/) for Eureka
- [Apache Kafka](https://kafka.apache.org/) for robust messaging
- All open-source contributors who make projects like this possible

---

<p align="center">
  <strong>Built with ❤️ by <a href="https://github.com/kaantopcuw">Kaan Topçu</a></strong>
</p>

<p align="center">
  <a href="https://github.com/kaantopcuw/NightFlow/issues">Report Bug</a>
  ·
  <a href="https://github.com/kaantopcuw/NightFlow/issues">Request Feature</a>
  ·
  <a href="https://github.com/kaantopcuw/NightFlow">⭐ Star this repo</a>
</p>
