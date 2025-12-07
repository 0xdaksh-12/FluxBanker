.PHONY: help install build dev dev-infra docker-up docker-down logs clean test test-infra test-docker api-local api-docker full-docker reset

help:
	@echo "FluxBanker Commands:"
	@echo "  install           Install all dependencies"
	@echo "  build             Build both API and App"
	@echo "  test              Test local run (H2)"
	@echo "  test-infra        Test runs on host with Docker services"
	@echo "  test-docker       Test runs in Docker with services"
	@echo "  api-local         Run API locally with Docker services"
	@echo "  app-dev           Run App locally"
	@echo "  dev               Run both API and App locally"
	@echo "  docker-up         Run full stack in Docker"
	@echo "  docker-down       Stop all Docker containers"
	@echo "  logs              Tail Docker logs"
	@echo "  clean             Clean build artifacts"
	@echo "  reset             Full reset: wipe volumes, clean, install, and run dev"

install:
	cd api && ./mvnw install -DskipTests
	cd app && pnpm install

build-api:
	cd api && ./mvnw package -DskipTests

build-app:
	cd app && pnpm build

build: build-api build-app

prod-build: build
	@echo "Production build complete. Ready for deployment."

# 1. Test local run (uses H2 in-memory)
test:
	cd api && ./mvnw test -Dspring.profiles.active=test

# 2. Test runs on host with Docker servers (requires dev-infra)
test-infra: dev-infra
	cd api && ./mvnw test -Dspring.profiles.active=test,test-infra

# 3. Test runs in Docker with services
test-docker: dev-infra
	docker compose run --rm tester ./mvnw test -Dspring.profiles.active=test,test-infra

# 4. Run local with Docker services (API on host, DB/Kafka in Docker)
api-local: dev-infra
	cd api && set -a && . ./.env && set +a && ./mvnw spring-boot:run -Dspring.mvc.pathmatch.matching-strategy=ant-path-matcher

# 5. Run App locally
app-dev:
	cd app && pnpm dev

# 6. Run API in Docker with Docker services (containerization for API)
api-docker:
	docker compose up -d --build api

# 7. Run Full Stack in Docker
full-docker:
	docker compose up -d --build

dev-infra:
	docker compose up -d postgres redis kafka prometheus grafana mailpit

docker-up: full-docker

docker-down:
	docker compose down

logs:
	docker compose logs -f

dev: dev-infra
	# Run API in background and App in foreground
	cd api && set -a && . ./.env && set +a && ./mvnw spring-boot:run -Dspring.mvc.pathmatch.matching-strategy=ant-path-matcher &
	cd app && pnpm dev

clean:
	cd api && ./mvnw clean
	cd app && rm -rf dist node_modules

reset:
	docker compose down -v
	$(MAKE) clean
	$(MAKE) install
	$(MAKE) dev
