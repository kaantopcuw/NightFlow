# 📦 Microservices Overview

| Service | Port | Description | DB | Tech Stack |
|---------|------|-------------|----|------------|
| **Config Server** | 8888 | Centralized configuration | - | Spring Cloud Config |
| **Discovery Server** | 8761 | Service registry | - | Netflix Eureka |
| **Gateway Service** | 8080 | API Gateway & Routing | - | Spring Cloud Gateway (Netty) |
| **Auth Service** | 8090 | Authentication & User Mgmt | PostgreSQL | JWT, Spring Security |
| **Venue Service** | 8091 | Venue & Organizer Mgmt | PostgreSQL | JPA |
| **Event Catalog** | 8092 | Event Browsing & Search | MongoDB | Redis Cache |
| **Ticket Service** | 8093 | Inventory & Reservations | PostgreSQL | Pessimistic Locking |
| **Shopping Cart** | 8094 | Temporary Basket | Redis | Redis Key-Value |
| **Order Service** | 8095 | Order Processing | PostgreSQL | Kafka Producer, Saga |
| **Notification** | 8096 | Email/SMS Notifications | - | Kafka Consumer |
| **Check-in Service** | 8097 | Entrance Control | Redis | QR Code, Redis Atomic |

## Service Dependencies

- **Auth Service**: Foundation for security. Most services validate tokens via Gateway or direct calls.
- **Event Catalog**: Depends on **Venue Service** for location data.
- **Order Service**: Depends on **Ticket Service** (confirm reservation) and **Cart Service** (clear cart).
- **Notification Service**: Listens to `order-created` topics from **Order Service**.
