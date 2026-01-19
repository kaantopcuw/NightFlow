# NightFlow E2E Tests

End-to-end integration tests for NightFlow microservices platform.

## Prerequisites

- All services running via `manage.sh`
- PostgreSQL, MongoDB, Redis available (via Docker)

## Quick Start

```bash
# 1. Start all services (from project root)
cd /path/to/NightFlow
./manage.sh start all

# 2. Run E2E tests
cd e2e-tests
./scripts/run-e2e-tests.sh

# Alternative: run with Maven directly
mvn verify -Dgateway.url=http://localhost:8080
```

## Test Scenarios

| Test Class | Description |
|------------|-------------|
| `FullFlowE2ETest` | Registration → Login → Tickets → Cart → Order |
| `CheckInFlowE2ETest` | Preload → Stats → Invalid ticket rejection |

## Architecture

```
┌─────────────┐      ┌─────────────┐      ┌─────────────────┐
│  E2E Tests  │─────►│   Gateway   │─────►│  Microservices  │
│ (RestAssured)│     │  :8080      │      │  (auth, order..)│
└─────────────┘      └─────────────┘      └─────────────────┘
```

All requests go through the Gateway, just like frontend would.

## Troubleshooting

If tests fail:
1. Check services: `./manage.sh status`
2. Check logs: `tail -f logs/<service>.log`
3. Ensure DBs have test data: `./scripts/run-e2e-tests.sh` seeds automatically
