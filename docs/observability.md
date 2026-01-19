# 🔭 Observability Guide

NightFlow uses the **LGTM Stack** (Loki, Grafana, Tempo, Micrometer) to provide comprehensive observability across all microservices.

## 🏗️ Architecture

| Component | Port | Purpose | Details |
|-----------|------|---------|---------|
| **Grafana** | `3000` | Visualization | Unified dashboard for metrics, logs, and traces. |
| **Prometheus**| `9090` | Metrics | Collects JVM, System, and Custom metrics from all services. |
| **Loki** | `3100` | Logs | Centralized logging system (like Prometheus, but for logs). |
| **Tempo** | `3200` | Tracing | Distributed tracing backend (OTLP compatible). |

## 🚀 Accessing Dashboards

1. **Grafana**: Open [http://localhost:3000](http://localhost:3000)
    - **User**: `admin`
    - **Password**: `password`
2. **Pre-built Dashboard**: Go to **Dashboards > General > NightFlow Service Overview**
    - Select a service from the **Application** dropdown (e.g., `order-service`).
    - View **Requests per Second**, **Latency**, and correlated **Logs**.

## 📊 Features

### 1. Distributed Tracing
Every request entering the API Gateway is assigned a unique `traceId`. This ID is propagated through all microservices, queues (Kafka), and databases.

- **How to Trace**:
    1. Find a `traceId` in the logs (e.g., `[order-service, 65b7c8d...]`).
    2. Go to **Explore** in Grafana.
    3. Select **Tempo** datasource.
    4. Enter the `traceId` to verify the full flow.

### 2. Metrics (Prometheus)
Services expose metrics at `/actuator/prometheus`.
- **Key Metrics**:
    - `http_server_requests_seconds`: Throughput and Latency.
    - `jvm_memory_used_bytes`: Memory usage.
    - `system_cpu_usage`: CPU usage.

### 3. Log Aggregation (Loki)
Logs are shipped directly from `Loki4jAppender` to Loki. 
- **LogQL Example**:
  ```logql
  {app="order-service"} |= "ERROR"
  ```
- **Correlation**: Logs are automatically linked to Traces via `traceId`.

## 🛠️ Configuration

### Local vs Production
- **Local**: 100% Sampling (`probability: 1.0`). All requests are traced.
- **Production**: Configurable sampling (e.g. 10%) to reduce overhead.

### Dependencies
All services utilize the following libraries for instrumentation:
- `micrometer-tracing-bridge-otel`
- `opentelemetry-exporter-otlp`
- `micrometer-registry-prometheus`
- `loki-logback-appender`
