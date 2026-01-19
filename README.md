# NightFlow - High-Scale Event Ticketing Platform

> **Status:** 🚧 Active Development (Alpha)

NightFlow is a robust, cloud-native microservices platform designed for high-concurrency event ticketing, venue management, and real-time order processing. Built with **Spring Boot 3** and modern cloud technologies, it demonstrates an enterprise-grade architecture capable of handling complex reservation flows and inventory management.

## 🚀 Architecture Overview

This project implements a fully distributed microservices architecture using the **Event-Driven** design pattern.

![Architecture Diagram](docs/architecture.png)

### Core Technologies
- **Backend:** Java 25, Spring Boot 3.3.0, Spring Cloud 2023
- **Databases:** PostgreSQL (Relational), MongoDB (Document), Redis (Cache/kv)
- **Message Broker:** Apache Kafka / RabbitMQ (Configuration dependant)
- **Service Discovery & Config:** Netflix Eureka, Spring Cloud Config
- **API Gateway:** Spring Cloud Gateway
- **Testing:** RestAssured (E2E), JUnit 5, TestContainers
- **Containerization:** Docker, Docker Compose

## 📦 Microservices Modules

Each service is independently versioned and maintained.

| Service | Description | Port | Repo |
| :--- | :--- | :--- | :--- |
| **[Auth Service](auth-service)** | JWT-based Authentication & User Management (OAuth2 ready) | `5000` | [Link](auth-service) |
| **[Event Catalog](event-catalog-service)** | MongoDB-backed Event listing, searching, and metadata | `5001` | [Link](event-catalog-service) |
| **[Ticket Service](ticket-service)** | Inventory management, reservation logic, concurrency handling | `5002` | [Link](ticket-service) |
| **[Order Service](order-service)** | Order processing, payment flows, invoice generation | `5003` | [Link](order-service) |
| **[Shopping Cart](shopping-cart-service)** | Redis-backed high-speed cart management | `5004` | [Link](shopping-cart-service) |
| **[Venue Service](venue-service)** | Venue layouts, seat mapping, organizer management | `5005` | [Link](venue-service) |
| **[Check-in Service](checkin-service)** | QR Code validation, fast-path entry management | `5007` | [Link](checkin-service) |
| **[Notification Service](notification-service)** | Async email/SMS notifications via Kafka events | `5006` | [Link](notification-service) |
| **[Gateway Service](gateway-service)** | Single entry point, routing, rate limiting | `8080` | [Link](gateway-service) |
| **[Discovery Server](discovery-server)** | Service registry (Eureka) | `8761` | [Link](discovery-server) |
| **[Config Server](config-server)** | Centralized configuration management | `8888` | [Link](config-server) |

## 🛠️ Getting Started

### Prerequisites
- JDK 25+
- Docker & Docker Compose
- Maven 3.9+

### Quick Start (Local)

1. **Clone the repository** (recursively if using submodules):
   ```bash
   git clone https://github.com/kaantopcu/NightFlow.git
   cd NightFlow
   ```

2. **Start Infrastructure**:
   Launch databases, message brokers, and discovery services.
   ```bash
   cd config-server
   docker-compose up -d
   ```

3. **Build & Run Services**:
   You can run individual services via Maven or use the provided scripts.
   ```bash
   # Run E2E test environment script
   ./e2e-tests/scripts/start-test-env.sh
   ```

4. **Verify System**:
   Access the **Discovery Dashboard** at [http://localhost:8761](http://localhost:8761) to see all registered services.

## 🧪 Testing

The project includes a comprehensive **End-to-End (E2E)** test suite located in `e2e-tests`. This suite validates the critical user journey:
1. User Registration & Login (JWT)
2. Browsing Events & Tickets
3. Adding to Cart (Inventory check)
4. Checkout & Order Creation
5. Ticket Verification (Check-in)

Run tests with:
```bash
./mvnw verify -pl e2e-tests
```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Built with ❤️ by [Kaan Topcu](https://github.com/kaantopcu).*
