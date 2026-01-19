#!/bin/bash

# ============================================
# NightFlow Application Environment Starter
# ============================================

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONFIG_SERVER_DIR="$SCRIPT_DIR/.."
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

echo "🚀 Starting NightFlow Application Environment..."

# Step 1: Ensure we're on the master branch in config-server for normal profile
echo "📋 Switching config-server to default path..."
cd "$CONFIG_SERVER_DIR"

# Step 2: Start Docker containers
echo "🐳 Starting Docker containers..."
docker-compose -f docker-compose.yml up -d

# Step 3: Wait for containers to be healthy
echo "⏳ Waiting for containers to be ready..."
sleep 10

# Check PostgreSQL
echo "   Checking PostgreSQL (port 5432)..."
until docker exec nightflow-postgres pg_isready -U postgres 2>/dev/null; do
    echo "   Waiting for PostgreSQL..."
    sleep 2
done
echo "   ✅ PostgreSQL is ready"

# Check Redis
echo "   Checking Redis (port 6379)..."
until docker exec nightflow-redis redis-cli ping 2>/dev/null | grep -q PONG; do
    echo "   Waiting for Redis..."
    sleep 2
done
echo "   ✅ Redis is ready"

# Check MongoDB
echo "   Checking MongoDB (port 27017)..."
until docker exec nightflow-mongodb mongosh --eval "db.adminCommand('ping')" 2>/dev/null | grep -q ok; do
    echo "   Waiting for MongoDB..."
    sleep 2
done
echo "   ✅ MongoDB is ready"

echo ""
echo "=========================================="
echo "✅ Application Environment is READY!"
echo ""
echo "Databases:"
echo "  PostgreSQL: localhost:5432"
echo "  MongoDB:    localhost:27017"
echo "  Redis:      localhost:6379"
echo "  Kafka:      localhost:9092"
echo ""
echo "Next steps:"
echo "  1. Start microservices using Maven or IDE"
echo "  2. Access the application via Gateway at http://localhost:8080"
echo "=========================================="
