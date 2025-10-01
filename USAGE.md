# Usage Guide

This guide covers how to run, develop, test, and operate FluxBanker day-to-day.

## Prerequisites

| Tool           | Minimum Version |
| -------------- | --------------- |
| Docker Engine  | 24+             |
| Docker Compose | v2              |
| Java           | 17              |
| Node.js        | 22.19.0         |
| pnpm           | 10.32.1         |

---

## Running the Full Stack (Recommended)

All infrastructure (Postgres, Redis, Kafka, Prometheus, Grafana) and the Spring Boot API are orchestrated via Docker Compose.

```bash
# 1. Copy and fill in environment variables
cp api/.env.example api/.env

# 2. Start everything
make up

# 3. In a second terminal, start the frontend
make dev-app
```

The application is now live at:

| Service         | URL                                        |
| --------------- | ------------------------------------------ |
| React SPA       | http://localhost:5173                      |
| Spring Boot API | http://localhost:8080                      |
| Swagger UI      | http://localhost:8080/swagger-ui.html      |
| Grafana         | http://localhost:3001 (admin / fluxbanker) |
| Prometheus      | http://localhost:9090                      |

---

## Running Services Individually

Use this approach when you want to iterate quickly on the backend without rebuilding Docker images.

```bash
# Start only infrastructure (DB, Redis, Kafka, monitoring)
make up

# Stop the API container so we can run it locally
docker compose -f api/docker-compose.yml stop app

# Run Spring Boot with hot reload
make dev-api

# Run the frontend dev server
make dev-app
```

---

## Makefile Reference

| Command            | Description                                         |
| ------------------ | --------------------------------------------------- |
| `make up`          | Start all Docker services (detached)                |
| `make up-watch`    | Start with Docker live-reload watch                 |
| `make down`        | Stop all services                                   |
| `make clean`       | Stop all services and wipe all volumes (full reset) |
| `make logs`        | Tail logs for all containers                        |
| `make restart-api` | Restart only the Spring Boot container              |
| `make dev-api`     | Run Spring Boot locally via Maven                   |
| `make dev-app`     | Run Vite + React dev server                         |
| `make build-api`   | Build the Spring Boot JAR (`-DskipTests`)           |
| `make grafana`     | Open Grafana in the browser                         |
| `make prometheus`  | Open Prometheus UI in the browser                   |
| `make swagger`     | Open Swagger UI in the browser                      |

---

## Environment Variables

Key variables in `api/.env`:

| Variable                | Description                                           |
| ----------------------- | ----------------------------------------------------- |
| `JWT_SECRET`            | HS256 secret for signing access tokens                |
| `ADMIN_EMAILS`          | Comma-separated list of emails auto-promoted to ADMIN |
| `GOOGLE_CLIENT_ID`      | Google OAuth2 client ID (feature currently stubbed)   |
| `GOOGLE_CLIENT_SECRET`  | Google OAuth2 client secret                           |
| `SPRING_DATASOURCE_URL` | JDBC connection string                                |
| `REDIS_HOST`            | Redis hostname                                        |
| `REDIS_PORT`            | Redis port                                            |

---

## Linting and Formatting

### Frontend

```bash
cd app

# Check for lint errors
pnpm run lint

# Auto-fix lint errors
pnpm run lint:fix

# Format all files with Prettier
pnpm run format
```

### Backend

```bash
cd api

# Apply Google Java Format to all source files
./mvnw spotless:apply

# Check formatting (runs automatically on `mvn verify`)
./mvnw spotless:check
```

---

## Running Tests

### Backend

```bash
cd api && ./mvnw test
```

### Frontend

There are currently no automated frontend tests. Contributions welcome.

---

## Full Reset

To wipe all data (Postgres volumes, Kafka offsets, Redis cache) and start fresh:

```bash
make clean
make up
```
