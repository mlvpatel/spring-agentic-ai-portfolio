# Spring Agentic AI portfolio

[![CI](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/workflows/ci.yml/badge.svg)](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](./pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-green.svg)](./pom.xml)

Java 21 Maven reactor of offline-first Spring Boot / Spring AI sample services behind a Spring Cloud Gateway edge. Default paths need no paid model keys. Live LLM wiring is optional per app.

## Architecture (C4)

### Context

```mermaid
C4Context
  title System context
  Person(dev, "Developer", "Calls portfolio APIs")
  System(portfolio, "Spring Agentic AI portfolio", "Gateway + domain apps")
  System_Ext(llm, "Optional LLM provider", "OpenAI / Ollama when enabled")
  Rel(dev, portfolio, "HTTPS + X-API-Key", "JSON")
  Rel(portfolio, llm, "Optional ChatClient", "when live mode is on")
```

### Containers

```mermaid
C4Container
  title Containers on the compose network
  Person(dev, "Developer")
  Container_Boundary(b, "Portfolio") {
    Container(gw, "ai-edge-gateway", "Spring Cloud Gateway", "Auth, rate limit, routes")
    Container(apps, "Domain apps P1–P10 + S1–S4", "Spring Boot 4 / Kotlin", "Offline engines")
    Container(prom, "Prometheus", "Metrics scrape")
    Container(graf, "Grafana", "Dashboards")
  }
  Rel(dev, gw, "HTTP :8080")
  Rel(gw, apps, "/api/** aliases and /svc/<app>/**")
  Rel(prom, apps, "scrape")
  Rel(graf, prom, "query")
```

## Modules

| ID | App | Port (internal) | Primary API |
|---|---|---|---|
| P1 | [yagni-copilot](apps/yagni-copilot/) | 8081 | `POST /api/v1/patch` |
| P2 | [design-rag-studio](apps/design-rag-studio/) | 8082 | ingest + generate (`/svc/design-rag-studio/...`) |
| P3 | [quality-gate](apps/quality-gate/) | 8083 | `POST /api/v1/gate` |
| P4 | [spec-orchestrator](apps/spec-orchestrator/) | 8084 | `POST /api/v1/runs` |
| P5 | [agent-observability](apps/agent-observability/) | 8085 | trajectories + analyze |
| P6 | [mcp-broker](apps/mcp-broker/) | 8086 | tools + invoke |
| P7 | [app-factory](apps/app-factory/) | 8087 | `POST /svc/app-factory/api/v1/generate` |
| P8 | [paper-algorithm-lab](apps/paper-algorithm-lab/) | 8088 | papers + synthesize |
| P9 | [system-architect](apps/system-architect/) | 8089 | `POST /api/v1/architect` |
| P10 | [ai-edge-gateway](apps/ai-edge-gateway/) | 8080 (host) | edge auth + routes |
| S1 | [multimodal-support-desk](apps/multimodal-support-desk/) | 8091 | `POST /api/v1/triage` |
| S2 | [kotlin-rag-microservice](apps/kotlin-rag-microservice/) | 8092 | ingest + query |
| S3 | [ai-validated-integration-harness](apps/ai-validated-integration-harness/) | 8093 | `POST /api/v1/validate` |
| S4 | [security-review-assistant](apps/security-review-assistant/) | 8094 | `POST /api/v1/review` |

Shared library: [libs/shared](libs/shared/). Infra: [infra/](infra/) (compose, helm, monitoring).

## Run the sandbox

```bash
./mvnw -DskipTests package
cp infra/compose/.env.example infra/compose/.env
# set GATEWAY_SECURITY_APIKEY and matching per-app keys (env file only)
cd infra/compose && docker compose --env-file .env up -d
```

Gateway listens on host `:8080`. Prefer unique path aliases (`/api/v1/gate`, `/api/v1/patch`, …) or `/svc/<app>/api/v1/...` when paths collide (`generate`, `ingest`).

Stress notes from a laptop Docker run: [docs/stress-results.md](docs/stress-results.md).

## Security model

- Edge requires `X-API-Key` or `Authorization: Bearer` equal to `GATEWAY_SECURITY_APIKEY`. JWT-looking `ey*` strings and `valid-test-token` are not bypasses.
- Each app also checks its own API key env var.
- Token-bucket rate limit on the gateway (defaults: replenish 100/s, burst 200).
- Quality gate fails sources that match a hardcoded-secret pattern.
- Design RAG and Kotlin RAG refuse empty corpora instead of inventing content.

## Build and test

```bash
./mvnw -B -DskipITs test
```

## License

Apache License 2.0. See [LICENSE](./LICENSE).
