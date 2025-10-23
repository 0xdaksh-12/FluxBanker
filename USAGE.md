# Usage Guide

This guide covers how to run, develop, test, and operate FluxBanker.

## Prerequisites

| Tool           | Minimum Version |
| -------------- | --------------- |
| Docker Engine  | 24+             |
| Docker Compose | v2              |
| Java           | 17              |
| Node.js        | 22.19.0         |
| pnpm           | 10.32.1         |

---

## Running the Full Stack

FluxBanker uses a root `Makefile` to orchestrate both the Spring Boot API and the React frontend along with all supporting infrastructure.

### 1. Setup Environment
```bash
cp api/.env.example api/.env
```

### 2. Install Dependencies
```bash
make install
```

### 3. Start Development Mode
This starts all Docker services (Postgres, Redis, Kafka, Prometheus, Grafana) and runs both the API and App locally.
```bash
make dev
```

The application is now live at:

| Service         | URL                                   |
| --------------- | ------------------------------------- |
| React SPA       | http://localhost:5173                 |
| Spring Boot API | http://localhost:8080                 |
| Swagger UI      | http://localhost:8080/swagger-ui.html |
| Grafana         | http://localhost:3000 (admin / admin) |
| Prometheus      | http://localhost:9090                 |

---

## Testing Modes

FluxBanker is built for stability and supports three distinct execution modes for its test suite:

### 1. Local (In-Memory H2)
Fastest mode, ideal for quick feedback loops. Disables Kafka and Redis.
```bash
make test
```

### 2. Host-to-Container
Runs tests on your host machine against real Docker-hosted infrastructure.
```bash
make test-infra
```

### 3. Full Containerized
Runs the entire test suite *inside* a Docker container, ensuring identical results between local dev and CI.
```bash
make test-docker
```

---

## Makefile Reference

| Command        | Description                                           |
| -------------- | ----------------------------------------------------- |
| `make install` | Install all API and App dependencies                  |
| `make dev`     | Run full stack locally with Docker infra              |
| `make api-docker` | Run API fully containerized                        |
| `make test`    | Run local H2 tests                                    |
| `make test-infra` | Run host tests against Docker infra                |
| `make test-docker` | Run tests inside Docker container                 |
| `make docker-down` | Stop all Docker containers                        |
| `make logs`    | Tail Docker logs                                      |
| `make clean`   | Clean build artifacts and node_modules                |

---

## Observability & Monitoring

FluxBanker exposes high-fidelity business metrics:
- **Ledger Health**: Transaction volumes, account provisioning rates.
- **System Metrics**: JVM performance, Kafka lag, Redis hit rates.

Access the pre-configured dashboards in Grafana at `http://localhost:3000`.

---

## Full Reset

To wipe all data (Postgres volumes, Kafka offsets, Redis cache) and start fresh:

```bash
make docker-down
make clean
make dev
```
