#!/bin/bash

# ============================================
# NightFlow E2E Test Runner
# ============================================
# Çalışan servislere karşı E2E testlerini yürütür.
# Gateway üzerinden tüm istekleri yönlendirir.
# ============================================

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"
E2E_DIR="$SCRIPT_DIR/.."

# Renkler
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================"
echo "  NightFlow E2E Test Runner"
echo "========================================"
echo ""

# ─────────────── STEP 1: Check Services ───────────────
echo "📋 Checking if services are running..."

check_service() {
    local name=$1
    local port=$2
    if curl -s "http://localhost:$port/actuator/health" | grep -q "UP" 2>/dev/null; then
        echo -e "   ✅ $name (port $port)"
        return 0
    else
        echo -e "   ${RED}❌ $name (port $port) - NOT RUNNING${NC}"
        return 1
    fi
}

SERVICES_OK=true

check_service "Gateway" 8080 || SERVICES_OK=false
check_service "Auth Service" 8090 || SERVICES_OK=false
check_service "Venue Service" 8091 || SERVICES_OK=false
check_service "Event Catalog" 8092 || SERVICES_OK=false
check_service "Ticket Service" 8093 || SERVICES_OK=false
check_service "Cart Service" 8094 || SERVICES_OK=false
check_service "Order Service" 8095 || SERVICES_OK=false
check_service "Checkin Service" 8097 || SERVICES_OK=false

if [ "$SERVICES_OK" = false ]; then
    echo ""
    echo -e "${RED}❌ Some services are not running!${NC}"
    echo ""
    echo "Start services first:"
    echo "   cd $PROJECT_ROOT && ./manage.sh start all"
    echo ""
    exit 1
fi

echo ""
echo -e "${GREEN}✅ All services are running!${NC}"
echo ""

# ─────────────── STEP 2: Seed Test Data ───────────────
echo "🌱 Seeding test data..."

# PostgreSQL'e test venue, event, ticket ekle
PGPASSWORD=password psql -h localhost -U postgres -d nightflow_venues -f "$SCRIPT_DIR/seed-venues.sql" 2>/dev/null || echo "   (Venues already seeded or no changes)"
PGPASSWORD=password psql -h localhost -U postgres -d nightflow_tickets -f "$SCRIPT_DIR/seed-tickets.sql" 2>/dev/null || echo "   (Tickets already seeded or no changes)"

echo -e "   ${GREEN}✅ Test data ready${NC}"
echo ""

# ─────────────── STEP 3: Run Tests ───────────────
echo "🧪 Running E2E Tests..."
echo ""

cd "$E2E_DIR"
./mvnw verify -Dgateway.url=http://localhost:8080

TEST_RESULT=$?

echo ""
echo "========================================"
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}✅ All E2E Tests PASSED!${NC}"
else
    echo -e "${RED}❌ Some tests FAILED!${NC}"
fi
echo "========================================"

exit $TEST_RESULT
