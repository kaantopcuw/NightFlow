<p align="center">
  <img src="https://img.shields.io/badge/SpringDoc_OpenAPI-2.8.6-85EA2D?style=for-the-badge&logo=swagger&logoColor=white" alt="OpenAPI">
  <img src="https://img.shields.io/badge/REST_API-JSON-blue?style=for-the-badge&logo=json&logoColor=white" alt="REST">
</p>

# 📡 API Reference & Documentation

> Complete API documentation for all NightFlow microservices

---

## 🌐 Centralized API Gateway

All API requests should go through the **API Gateway** for proper routing, load balancing, and authentication:

```
┌─────────────────────────────────────────────────────────────┐
│                     API GATEWAY                             │
│                  http://localhost:8080                      │
├─────────────────────────────────────────────────────────────┤
│  /api/auth/**     → Auth Service (8090)                     │
│  /api/venues/**   → Venue Service (8091)                    │
│  /api/events/**   → Event Catalog (8092)                    │
│  /api/tickets/**  → Ticket Service (8093)                   │
│  /api/cart/**     → Shopping Cart (8094)                    │
│  /api/orders/**   → Order Service (8095)                    │
│  /api/notify/**   → Notification Service (8096)             │
│  /api/checkin/**  → Check-in Service (8097)                 │
└─────────────────────────────────────────────────────────────┘
```

### Gateway Swagger UI

👉 **[http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)**

> **Note**: Select the specific service definition from the dropdown menu to view its endpoints.

---

## 📖 Per-Service Swagger Documentation

Each microservice exposes its own interactive API documentation:

<table>
<thead>
<tr>
<th align="center">🔗</th>
<th align="left">Service</th>
<th align="center">Port</th>
<th align="left">Swagger UI URL</th>
</tr>
</thead>
<tbody>

<tr>
<td align="center">🔐</td>
<td><strong>Auth Service</strong></td>
<td align="center"><code>8090</code></td>
<td><a href="http://localhost:8090/swagger-ui.html">http://localhost:8090/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">🏟️</td>
<td><strong>Venue Service</strong></td>
<td align="center"><code>8091</code></td>
<td><a href="http://localhost:8091/swagger-ui.html">http://localhost:8091/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">📅</td>
<td><strong>Event Catalog</strong></td>
<td align="center"><code>8092</code></td>
<td><a href="http://localhost:8092/swagger-ui.html">http://localhost:8092/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">🎟️</td>
<td><strong>Ticket Service</strong></td>
<td align="center"><code>8093</code></td>
<td><a href="http://localhost:8093/swagger-ui.html">http://localhost:8093/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">🛒</td>
<td><strong>Shopping Cart</strong></td>
<td align="center"><code>8094</code></td>
<td><a href="http://localhost:8094/swagger-ui.html">http://localhost:8094/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">📋</td>
<td><strong>Order Service</strong></td>
<td align="center"><code>8095</code></td>
<td><a href="http://localhost:8095/swagger-ui.html">http://localhost:8095/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">📧</td>
<td><strong>Notification</strong></td>
<td align="center"><code>8096</code></td>
<td><a href="http://localhost:8096/swagger-ui.html">http://localhost:8096/swagger-ui.html</a></td>
</tr>

<tr>
<td align="center">✅</td>
<td><strong>Check-in</strong></td>
<td align="center"><code>8097</code></td>
<td><a href="http://localhost:8097/swagger-ui.html">http://localhost:8097/swagger-ui.html</a></td>
</tr>

</tbody>
</table>

---

## 🔑 Authentication

NightFlow uses **JWT (JSON Web Token)** for authentication. Here's the flow:

```
┌──────────┐         ┌──────────────┐         ┌──────────────┐
│  Client  │──POST──▶│ Auth Service │──JWT───▶│   Gateway    │
└──────────┘  /login └──────────────┘         └──────┬───────┘
                                                     │
                                              Authorization:
                                              Bearer <token>
                                                     │
                                                     ▼
                                          ┌──────────────────┐
                                          │ Protected APIs   │
                                          └──────────────────┘
```

### Get JWT Token

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login and get JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

### Use JWT Token

```bash
# Access protected endpoints
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📬 Service API Endpoints

### Auth Service (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/login` | login |
| `POST` | `/register` | register |
| `GET` | `/test` | test |

### Venue Service (`/api/venues`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/organizers` | findAll_1 |
| `POST` | `/organizers` | create_1 |
| `GET` | `/organizers/slug/{slug}` | findBySlug |
| `GET` | `/organizers/{id}` | findById_1 |
| `PUT` | `/organizers/{id}` | update_1 |
| `DELETE` | `/organizers/{id}` | delete_1 |
| `PATCH` | `/organizers/{id}/status` | updateStatus |
| `GET` | `/venues` | findAll |
| `POST` | `/venues` | create |
| `GET` | `/venues/city/{city}` | findByCity |
| `PATCH` | `/venues/reservations/{reservationId}/cancel` | cancelReservation |
| `PATCH` | `/venues/reservations/{reservationId}/confirm` | confirmReservation |
| `GET` | `/venues/search` | search |
| `GET` | `/venues/type/{type}` | findByType |
| `GET` | `/venues/{id}` | findById |
| `PUT` | `/venues/{id}` | update |
| `DELETE` | `/venues/{id}` | delete |
| `GET` | `/venues/{venueId}/reservations` | getReservations |
| `POST` | `/venues/{venueId}/reserve` | reserve |

### Event Catalog Service (`/api/events`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/events` | findAll |
| `POST` | `/events` | create |
| `GET` | `/events/category/{category}` | findByCategory |
| `GET` | `/events/city/{city}` | findByCity |
| `GET` | `/events/featured` | findFeatured |
| `GET` | `/events/slug/{slug}` | findBySlug |
| `GET` | `/events/upcoming` | findUpcoming |
| `GET` | `/events/{id}` | findById |
| `PUT` | `/events/{id}` | update |
| `DELETE` | `/events/{id}` | delete |
| `PATCH` | `/events/{id}/status` | updateStatus |

### Ticket Service (`/api/tickets`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/ticket-categories` | create |
| `GET` | `/ticket-categories/event/{eventId}` | findByEventId |
| `GET` | `/ticket-categories/{id}` | findById |
| `PUT` | `/ticket-categories/{id}` | update |
| `POST` | `/tickets/confirm-sale` | confirmSale |
| `GET` | `/tickets/event/{eventId}` | getCategoriesByEvent |
| `GET` | `/tickets/event/{eventId}/all` | getAllTicketsByEvent |
| `POST` | `/tickets/reserve` | reserveTickets |
| `DELETE` | `/tickets/reserve/{sessionId}` | cancelReservation |
| `GET` | `/tickets/{ticketCode}` | getTicketByCode |
| `PATCH` | `/tickets/{ticketCode}/checkin` | markAsCheckedIn |

### Shopping Cart Service (`/api/cart`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/cart/add` | addToCart |
| `GET` | `/cart/{sessionId}` | getCart |
| `DELETE` | `/cart/{sessionId}` | clearCart |
| `DELETE` | `/cart/{sessionId}/item/{index}` | removeFromCart |

### Order Service (`/api/orders`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/orders` | createOrder |
| `GET` | `/orders/{orderNumber}` | getOrder |
| `POST` | `/orders/{orderNumber}/pay` | payOrder |

### Checkin Service (`/api/checkin`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/checkin/event/{eventId}/preload` | preloadEventTickets |
| `GET` | `/checkin/event/{eventId}/stats` | getEventStats |
| `GET` | `/checkin/health` | health |
| `GET` | `/checkin/ticket/{ticketCode}` | getTicketStatus |
| `POST` | `/checkin/validate` | validateAndCheckIn |

### Notification Service (`/api/notifications`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/notifications/health` | health |

---

## 🧪 Testing with cURL

### Complete Flow Example

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!","firstName":"Test","lastName":"User"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!"}' | jq -r '.token')

# 3. Browse events
curl http://localhost:8080/api/events \
  -H "Authorization: Bearer $TOKEN"

# 4. Add to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ticketId":"ticket-123","quantity":2}'

# 5. Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## 📦 Postman Collection

For convenient API testing, import the Postman collection:

```
docs/NightFlow.postman_collection.json
```

> **Tip**: Set the `baseUrl` environment variable to `http://localhost:8080` and `token` variable after login.

---

## 📝 Response Formats

All APIs return JSON responses with consistent structure:

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-01-19T21:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid email format",
    "details": [...]
  },
  "timestamp": "2026-01-19T21:30:00Z"
}
```

---

<p align="center">
  <a href="../README.md">← Back to README</a>
</p>
