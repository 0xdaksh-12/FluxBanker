# Observability & Monitoring

FluxBanker includes a full monitoring stack using Prometheus and Grafana.

## Prometheus Scrape

Prometheus is configured to scrape the Spring Boot `/actuator/prometheus` endpoint every 5 seconds.

```mermaid
sequenceDiagram
    participant Grafana
    participant Prometheus
    participant Spring API Actuator

    Prometheus->>Spring API Actuator: GET /actuator/prometheus
    Spring API Actuator-->>Prometheus: Return micrometer metrics (JVM, Tomcat, custom)

    Grafana->>Prometheus: Execute PromQL query
    Prometheus-->>Grafana: Return time-series data
```

## Custom Metrics (`BankingMetrics.java`)

We expose domain-specific metrics to Prometheus via Micrometer:

1. `banking.accounts.active` (Gauge): Total provisioned accounts in Postgres.
2. `banking.transfers.total` (Counter): Total transfers, tagged by `type` and `status`.

## Grafana Dashboards

Grafana is provisioned automatically.

- **URL**: `http://localhost:3001`
- **Login**: `admin` / `fluxbanker`

Dashboards can be imported directly within Grafana using community IDs (e.g. `11378` or `4701` for JVM Micrometer dashboards) to instantly visualize Spring Boot metrics.
