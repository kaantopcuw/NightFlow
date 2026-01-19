<p align="center">
  <img src="docs/nightflow-banner.png" alt="NightFlow Banner" width="600">
</p>

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
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.1-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.0.1">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.1.0-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=flat-square" alt="Status">
</p>

---

## 🌟 What is NightFlow?

**NightFlow** is a production-ready, cloud-native microservices platform designed to handle high-concurrency event ticketing, venue management, and real-time order processing. Think of it as the backbone for concerts, festivals, sports events, and more – built to handle thousands of simultaneous ticket purchases without breaking a sweat.

This project showcases modern backend engineering practices including event-driven architecture, distributed caching, saga patterns, and comprehensive E2E testing. Whether you're learning microservices or building your own ticketing system, NightFlow provides a solid foundation.

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
             ┌──────────▼──────────┐          │           ┌──────────▼──────────┐
             │    API Gateway      │◄─────────┘           │   Message Broker    │
             │      (8080)         │                      │   Apache Kafka      │
             └──────────┬──────────┘                      └──────────┬──────────┘
                        │                                            │
    ┌───────────────────┼───────────────────┬───────────────────────┤
    │                   │                   │                       │
┌───▼───┐  ┌───────────▼───────┐  ┌───────▼───────┐  ┌─────────────▼─────────────┐
│ Auth  │  │  Event Catalog    │  │    Ticket     │  │      Order Service        │
│(8090) │  │     (8092)        │  │    (8093)     │  │        (8095)             │
│       │  │                   │  │               │  │                           │
│ JWT   │  │ MongoDB + Redis   │  │  PostgreSQL   │  │  Saga + Kafka Producer    │
└───┬───┘  └───────────────────┘  └───────┬───────┘  └─────────────┬─────────────┘
    │                                     │                        │
    │      ┌───────────┐    ┌─────────────▼───────┐    ┌──────────▼──────────┐
    │      │   Venue   │    │   Shopping Cart     │    │  Notification       │
    └──────►  (8091)   │    │      (8094)         │    │     (8096)          │
           │           │    │                     │    │                     │
           │PostgreSQL │    │  Redis + Feign      │    │ Kafka Consumer+Mail │
           └───────────┘    └─────────────────────┘    └─────────────────────┘
                                                              
                            ┌─────────────────────┐
                            │   Check-in Service  │
                            │      (8097)         │
                            │                     │
                            │  Redis + QR Scan    │
                            └─────────────────────┘
```

### 📡 Data Flow

```
User → Gateway → Auth → Event Catalog → Ticket → Cart → Order → Kafka → Notification
                                           │                              │
                                           └──── Check-in (Event Day) ────┘
```

---

## 📦 Microservices

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

## 🔭 Observability

NightFlow implements a complete **LGTM Stack** for monitoring:

| Tool | Port | Description |
|------|------|-------------|
| **Grafana** | `3000` | Visualization Dashboard (User: `admin` / Pass: `password`) |
| **Prometheus**| `9090` | Metrics Collection |
| **Loki** | `3100` | Centralized Logging |
| **Tempo** | `3200` | Distributed Tracing (OTLP) |

👉 **[View Observability Guide](docs/observability.md)** for detailed usage instructions.

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
<td>Order processing with distributed transaction management</td>
<td align="center"><code>8095</code></td>
<td>Saga Pattern, Kafka Producer, PostgreSQL</td>
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
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
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
├── 📂 order-service/         # Order processing (Saga)
├── 📂 notification-service/  # Email/SMS (Kafka Consumer)
├── 📂 checkin-service/       # QR validation (Redis)
├── 📂 e2e-tests/             # End-to-end test suite
└── 📂 docs/                  # Documentation & diagrams
```

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
