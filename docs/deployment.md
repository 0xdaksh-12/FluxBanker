# Deployment Guide

FluxBanker is designed to be easily deployed via Docker Compose.

## Prerequisites

- Docker Engine 24+
- Docker Compose v2
- Java 17 (if running Spring natively)
- Node.js 22 (if running Vite natively)

## Production Deployment

1. Clone the repository on your server.
2. Copy `.env.example` to `.env` and fill in the required JWT secrets.
3. Build and start the entire stack:

```bash
docker compose -f api/docker-compose.yml up -d --build
```

This will spin up:

- `fluxbanker-api` (Port 8080)
- `fluxbanker-db` (Postgres, Port 5432)
- `fluxbanker-redis` (Port 6379)
- `fluxbanker-kafka` (Port 9092)
- `fluxbanker-prometheus` (Port 9090)
- `fluxbanker-grafana` (Port 3001)

## Frontend Deployment

The Vite SPA (`/app`) should be built and served statically (e.g., via Nginx, Vercel, or AWS S3/CloudFront) in a production environment.

```bash
cd app
pnpm install
pnpm run build
```

Serve the resulting `dist/` directory. Ensure that the web server is configured to rewrite all paths to `index.html` (for client-side routing).
