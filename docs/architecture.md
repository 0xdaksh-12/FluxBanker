# Architecture Overview

FluxBanker is an enterprise-grade banking platform simulator built with a clear separation of concerns.

## Tech Stack

- **Frontend**: Vite, React 19, Zustand, React Query, Axios, Recharts.
- **Backend API**: Java 17, Spring Boot 3.
- **Database**: PostgreSQL (Flyway migrations).
- **Caching**: Redis.
- **Event Streaming**: Apache Kafka (KRaft mode).
- **Observability**: Prometheus & Grafana.

## Core Features Implemented

- **Double-Entry Ledger**: Ensures strict debit/credit equality.
- **RBAC (Role-Based Access Control)**: Enforces `USER` and `ADMIN` roles, offering an exclusive Admin Dashboard for global oversight.
- **Simulate Deposit**: Secure API to provision initial account liquidity for testing without requiring Plaid integrations.

## System Topology

```mermaid
graph TD
    Client[React SPA] -->|REST API| API(Spring Boot App)

    subgraph Infrastructure
        API -->|Read/Write| DB[(PostgreSQL)]
        API -->|Cache Account Data| Cache[(Redis)]
        API -->|Produce Audit Events| Kafka[Kafka Broker]

        Prometheus([Prometheus]) -.->|Scrape /actuator| API
        Grafana([Grafana]) -.->|Query| Prometheus
    end

    Kafka -->|Consume Audit Events| API
```

## Data Flow: Money Transfer

```mermaid
sequenceDiagram
    participant User
    participant React UI
    participant Spring API
    participant PostgreSQL
    participant Kafka

    User->>React UI: Submit Transfer ($500)
    React UI->>Spring API: POST /transactions/transfer

    activate Spring API
    Spring API->>PostgreSQL: BEGIN TRANSACTION
    Spring API->>PostgreSQL: SELECT source_account FOR UPDATE
    Spring API->>PostgreSQL: SELECT dest_account FOR UPDATE

    alt Insufficient Funds
        Spring API->>PostgreSQL: ROLLBACK
        Spring API-->>React UI: 400 Bad Request
    else Sufficient Funds
        Spring API->>PostgreSQL: UPDATE source_account (balance - $500)
        Spring API->>PostgreSQL: UPDATE dest_account (balance + $500)
        Spring API->>PostgreSQL: INSERT 2x Transactions (Debit/Credit)
        Spring API->>PostgreSQL: COMMIT

        Spring API->>Kafka: PUBLISH TransactionEvent
        Spring API-->>React UI: 200 OK
    end
    deactivate Spring API

    Kafka-->>Spring API: ASYNC Consume TransactionEvent
    Spring API->>Spring API: Log structured audit entry
    Spring API->>Spring API: Increment Micrometer metrics
```
