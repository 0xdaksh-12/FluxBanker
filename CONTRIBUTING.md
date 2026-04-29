# Contributing to FluxBanker

Thank you for your interest in contributing to FluxBanker!

## Local Development Setup

We use a root `Makefile` to simplify local development.

1. Start backing services (Postgres, Redis, Kafka, Prometheus, Grafana):

   ```bash
   make up
   ```

2. Start the Spring Boot API:

   ```bash
   make dev-api
   ```

3. Start the Vite React Frontend:
   ```bash
   make dev-app
   ```

## Commit Message Guidelines

We follow the **Google-style commit message format**:

```
type(scope): subject
```

Types: `feat`, `fix`, `refactor`, `docs`, `chore`.
Example: `feat(api): add redis caching to account service`

## Code Style

- **Java**: Follow Google Java Style Guide. Run `./mvnw spotless:apply` to format.
- **Frontend**: Follow ESLint and Prettier configurations.
- **CSS**: Pure vanilla CSS using design system tokens. Do not introduce Tailwind.

## Design System: Swiss Poster Bento

We follow a strict "Swiss Poster" aesthetic inspired by mid-century graphic design and modern "Bento" layouts.

- **Typography**: Inter (Sans) for headers, IBM Plex Mono for numerical data and system status.
- **Color Palette**: High contrast. Primary use of Black (`#000000`) and White (`#FFFFFF`) with functional accents (e.g., `#166534` for positive growth).
- **Borders**: Sharp edges only (`border-radius: 0`). Thick borders (`var(--bw-thick)`) for core containers.
- **Shadows**: Hard shadows only. Avoid blurs. Use `box-shadow: 8px 8px 0px var(--ink)`.
- **Components**: Use CSS Modules (`.module.css`) to prevent style leakage.

## Documentation Requirements

- **API Changes**: Update `API_REFERENCE.md` if you add or modify endpoints.
- **Comments**: Use Google-style Javadoc for backend methods and clean, minimal comments for frontend logic.
- **PRs**: Ensure all tests pass (`make test`) before submitting.
