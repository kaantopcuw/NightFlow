#!/bin/bash

# ============================================
# 🎮 NightFlow Service Manager
# ============================================
# Usage:
#   ./manage.sh start [all|infra|service_name]
#   ./manage.sh stop [all|infra|service_name]
#   ./manage.sh restart [all|service_name]
#   ./manage.sh status
#   ./manage.sh logs [service_name]
# ============================================

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directories
PROJECT_ROOT="$(pwd)"
LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/.run"
CONFIG_SERVER_DIR="$PROJECT_ROOT/config-server"

# Ensure directories exist
mkdir -p "$LOG_DIR"
mkdir -p "$PID_DIR"

# Deployment Order (for 'start all')
ORDERED_SERVICES=(
    "config-server"
    "discovery-server"
    "gateway-service"
    "auth-service"
    "venue-service"
    "event-catalog-service"
    "ticket-service"
    "shopping-cart-service"
    "order-service"
    "notification-service"
    "checkin-service"
)

# ============================================
# Helper Functions
# ============================================

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

get_port() {
    case "$1" in
        "config-server") echo 8888 ;;
        "discovery-server") echo 8761 ;;
        "gateway-service") echo 8080 ;;
        "auth-service") echo 8090 ;;
        "venue-service") echo 8091 ;;
        "event-catalog-service") echo 8092 ;;
        "ticket-service") echo 8093 ;;
        "shopping-cart-service") echo 8094 ;;
        "order-service") echo 8095 ;;
        "notification-service") echo 8096 ;;
        "checkin-service") echo 8097 ;;
        *) echo "" ;;
    esac
}

check_port() {
    lsof -i ":$1" >/dev/null 2>&1
}

wait_for_port() {
    local service=$1
    local port=$2
    local retries=30
    local wait=2

    echo -n "   Waiting for $service ($port)..."
    for ((i=0; i<retries; i++)); do
        if check_port "$port"; then
            echo -e "${GREEN} OK${NC}"
            return 0
        fi
        echo -n "."
        sleep $wait
    done
    echo -e "${RED} FAILED${NC}"
    return 1
}

start_infra() {
    log_info "Starting Infrastructure (Docker)..."
    cd "$CONFIG_SERVER_DIR"
    docker-compose up -d
    cd "$PROJECT_ROOT"
    log_success "Infrastructure is running."
}

stop_infra() {
    log_info "Stopping Infrastructure (Docker)..."
    cd "$CONFIG_SERVER_DIR"
    docker-compose stop
    cd "$PROJECT_ROOT"
    log_success "Infrastructure stopped."
}

start_service_process() {
    local service=$1
    local port=$(get_port "$service")
    local pid_file="$PID_DIR/$service.pid"
    local log_file="$LOG_DIR/$service.log"

    if [ -z "$port" ]; then
        log_error "Unknown service: $service"
        return 1
    fi

    if [ -f "$pid_file" ] && kill -0 $(cat "$pid_file") 2>/dev/null; then
        log_warn "$service is already running (PID: $(cat "$pid_file"))"
        return
    fi

    echo -e "   ▶ Starting ${GREEN}$service${NC} on port $port..."
    
    # Run Maven in background
    nohup ./mvnw -f "$PROJECT_ROOT/$service/pom.xml" spring-boot:run \
        -Dspring-boot.run.jvmArguments="-Dserver.port=$port" \
        > "$log_file" 2>&1 &
    
    local pid=$!
    echo $pid > "$pid_file"
    echo "      PID: $pid | Log: logs/$service.log"
}

stop_service_process() {
    local service=$1
    local pid_file="$PID_DIR/$service.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            echo -n "   ⏹ Stopping $service (PID: $pid)..."
            kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null
            rm "$pid_file"
            echo -e "${GREEN} DONE${NC}"
        else
            log_warn "$service PID file exists but process is dead. Cleaning up."
            rm "$pid_file"
        fi
    else
        log_warn "$service is not running (no PID file)."
    fi
}

start_all() {
    start_infra
    
    for service in "${ORDERED_SERVICES[@]}"; do
        start_service_process "$service"
        
        # specific waits for crucial services
        if [ "$service" == "config-server" ]; then
            wait_for_port "config-server" 8888
            sleep 5 # Extra buffer for config server ready
        elif [ "$service" == "discovery-server" ]; then
            wait_for_port "discovery-server" 8761
            sleep 3
        fi
    done
    log_success "All services started!"
}

stop_all() {
    for (( idx=${#ORDERED_SERVICES[@]}-1 ; idx>=0 ; idx-- )) ; do
        stop_service_process "${ORDERED_SERVICES[idx]}"
    done
    log_success "All java services stopped."
}

show_status() {
    log_info "System Status:"
    echo "------------------------------------------------"
    printf "%-25s %-10s %-10s %-10s\n" "SERVICE" "PORT" "STATUS" "PID"
    echo "------------------------------------------------"
    
    for service in "${ORDERED_SERVICES[@]}"; do
        port=$(get_port "$service")
        pid_file="$PID_DIR/$service.pid"
        status="${RED}DOWN${NC}"
        pid="-"
        
        if [ -f "$pid_file" ] && kill -0 $(cat "$pid_file") 2>/dev/null; then
            status="${GREEN}UP${NC}"
            pid=$(cat "$pid_file")
        fi
        
        printf "%-35s %-10s %-15s %-10s\n" "$service" "$port" "$status" "$pid"
    done
}

# ============================================
# Main Logic
# ============================================

COMMAND=$1
TARGET=$2

case "$COMMAND" in
    start)
        if [ "$TARGET" == "all" ]; then
            start_all
            show_status
        elif [ "$TARGET" == "infra" ]; then
            start_infra
        else
            port=$(get_port "$TARGET")
            if [ -n "$port" ]; then
                start_infra # Ensure infra is up just in case
                start_service_process "$TARGET"
            else
                log_error "Unknown target: $TARGET. Usage: ./manage.sh start [all|infra|service_name]"
                exit 1
            fi
        fi
        ;;

    stop)
        if [ "$TARGET" == "all" ]; then
            stop_all
            stop_infra
            show_status
        elif [ "$TARGET" == "infra" ]; then
            stop_infra
        else
            port=$(get_port "$TARGET")
            if [ -n "$port" ]; then
                stop_service_process "$TARGET"
            else
                log_error "Unknown target: $TARGET. Usage: ./manage.sh stop [all|infra|service_name]"
                exit 1
            fi
        fi
        ;;

    restart)
        if [ "$TARGET" == "all" ]; then
            stop_all
            start_all
            show_status
        elif [ "$TARGET" == "infra" ]; then
            stop_infra
            start_infra
        else
            port=$(get_port "$TARGET")
            if [ -n "$port" ]; then
                stop_service_process "$TARGET"
                sleep 2
                start_service_process "$TARGET"
            else
                log_error "Unknown target: $TARGET. Usage: ./manage.sh restart [all|service_name]"
                exit 1
            fi
        fi
        ;;

    status)
        show_status
        ;;

    logs)
        port=$(get_port "$TARGET")
        if [ -n "$port" ]; then
            tail -f "$LOG_DIR/$TARGET.log"
        else
            log_error "Unknown service: $TARGET"
            exit 1
        fi
        ;;

    *)
        echo "Usage: $0 {start|stop|restart|status|logs} [target]"
        echo "  Targets: all, infra, <service-name>"
        exit 1
        ;;
esac
