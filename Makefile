.PHONY: help install build dev dev-infra docker-up docker-down logs clean

help:
	@echo "FluxBanker Monorepo Commands:"
	@echo "  install      Install all dependencies (Maven & pnpm)"
	@echo "  build        Build the entire project"
	@echo "  dev          Run both API and App locally (starts infra in Docker automatically)"
	@echo "  dev-infra    Start only infrastructure in Docker (Postgres, Redis, Kafka, Prometheus, Grafana)"
	@echo "  docker-up    Start everything (API + Infra) in Docker"
	@echo "  docker-down  Stop all Docker containers"
	@echo "  logs         Tail Docker logs"
	@echo "  clean        Clean all build artifacts"

install:
	cd api && ./mvnw install -DskipTests
	cd app && pnpm install

build:
	cd api && ./mvnw package -DskipTests
	cd app && pnpm build

dev-infra:
	cd api && docker compose up -d postgres redis kafka prometheus grafana

docker-up:
	cd api && docker compose up -d

docker-down:
	cd api && docker compose down

logs:
	cd api && docker compose logs -f

dev: dev-infra
	# Run API in background and App in foreground
	cd api && ./mvnw spring-boot:run &
	cd app && pnpm dev

clean:
	cd api && ./mvnw clean
	cd app && rm -rf dist node_modules
