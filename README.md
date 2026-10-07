# Spring agentic AI portfolio

[![CI](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/workflows/ci.yml/badge.svg)](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](./pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-green.svg)](./pom.xml)

Java 21 Maven reactor of offline-first Spring Boot / Spring AI sample services behind a Spring Cloud Gateway edge. Default paths need no paid model keys. `PortfolioAiClient` calls Spring AI `ChatClient` only when `OPENAI_API_KEY` is set and `SPRING_AI_MODEL_CHAT=openai`.

## Architecture (C4)

Archify 3.0.1 rendered [docs/architecture/portfolio.architecture.html](docs/architecture/portfolio.architecture.html) from commit bb31e6d. Notes are in [docs/architecture.md](docs/architecture.md).

### Context

```mermaid
C4Context
  title System context
  Person(dev, "Developer", "Calls portfolio APIs")
  System(portfolio, "Spring Agentic AI portfolio", "Gateway + domain apps")
  System_Ext(llm, "Optional LLM provider", "OpenAI when live mode is on")
  Rel(dev, portfolio, "HTTP or optional TLS + X-API-Key", "JSON")
  Rel(portfolio, llm, "ChatClient", "OPENAI_API_KEY + SPRING_AI_MODEL_CHAT=openai")
```

### Containers

```mermaid
C4Container
  title Containers on the compose network
  Person(dev, "Developer")
  Container_Boundary(b, "Portfolio") {
    Container(gw, "ai-edge-gateway", "Spring Cloud Gateway", "Auth, rate limit, unique routes")
    Container(apps, "Domain apps P1–P9 + S1–S4", "Spring Boot 4 / Kotlin", "Offline engines + optional AI")
    Container(redis, "Redis", "Gateway rate limit when REDIS_URL set")
    Container(pg, "Postgres", "Optional P2/P8 via compose profile")
  }
  Rel(dev, gw, "HTTP :8080")
  Rel(gw, apps, "/svc/<app>/** and unique /api/v1/... aliases")
  Rel(gw, redis, "token bucket")
  Rel(apps, pg, "JDBC when pgvector profile")
```

## Modules

| ID | App | Port (internal) | Primary API (via gateway) |
|---|---|---|---|
| P1 | [yagni-copilot](apps/yagni-copilot/) | 8081 | `POST /api/v1/patch` |
| P2 | [design-rag-studio](apps/design-rag-studio/) | 8082 | `POST /api/v1/design-rag/ingest` · `/api/v1/design-rag/generate` |
| P3 | [quality-gate](apps/quality-gate/) | 8083 | `POST /api/v1/gate` |
| P4 | [spec-orchestrator](apps/spec-orchestrator/) | 8084 | `POST /api/v1/runs` |
| P5 | [agent-observability](apps/agent-observability/) | 8085 | trajectories + analyze |
| P6 | [mcp-broker](apps/mcp-broker/) | 8086 | tools + invoke |
| P7 | [app-factory](apps/app-factory/) | 8087 | `POST /api/v1/app-factory/generate` |
| P8 | [paper-algorithm-lab](apps/paper-algorithm-lab/) | 8088 | papers + synthesize |
| P9 | [system-architect](apps/system-architect/) | 8089 | `POST /api/v1/architect` |
| P10 | [ai-edge-gateway](apps/ai-edge-gateway/) | 8080 (host) | edge auth + routes |
| S1 | [multimodal-support-desk](apps/multimodal-support-desk/) | 8091 | `POST /api/v1/triage` |
| S2 | [kotlin-rag-microservice](apps/kotlin-rag-microservice/) | 8092 | `POST /api/v1/kotlin-rag/ingest` · `/api/v1/query` |
| S3 | [ai-validated-integration-harness](apps/ai-validated-integration-harness/) | 8093 | `POST /api/v1/validate` |
| S4 | [security-review-assistant](apps/security-review-assistant/) | 8094 | `POST /api/v1/review` |

Shared library: [libs/shared](libs/shared/). Infra: [infra/](infra/) (compose, helm).

Bare `/api/v1/generate` and `/api/v1/ingest` are not routed (no catch-all). Use the unique aliases above or `/svc/<app>/api/v1/...`.

## Run the sandbox

```bash
./mvnw -DskipTests package
cp infra/compose/.env.example infra/compose/.env
# set GATEWAY_SECURITY_APIKEY, DESIGN_RAG_DB_PASSWORD, and PAPER_LAB_DB_PASSWORD
cd infra/compose
# Prod image build (Redis included; P2/P8 use H2 until Postgres profile is on):
docker compose -f docker-compose.yml --env-file .env up -d --build
# Fast jar-mount local loop (H2 for P2/P8):
docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env up -d
# Postgres for P2/P8 (pgvector JDBC):
docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile prod --env-file .env up -d --build
# Optional TLS on :8443 (generate certs first):
#   ../scripts/generate-dev-certs.sh && docker compose -f docker-compose.yml --env-file .env --profile tls up -d
```

Gateway listens on host `:8080`. Stress notes: [docs/stress-results.md](docs/stress-results.md).

## Security model

- Edge requires `X-API-Key` or `Authorization: Bearer` equal to `GATEWAY_SECURITY_APIKEY`. JWT-looking `ey*` strings and `valid-test-token` are not bypasses.
- Optional OIDC: set `OIDC_ISSUER_URI` so the gateway also accepts Bearer JWTs from that issuer. A valid token's `sub` claim is the rate-limit caller. Off when blank.
- Each app also checks its own API key env var.
- Rate limit: in-memory token bucket in unit tests; Redis token bucket (replenish rate and burst) when `REDIS_URL` is set (prod compose sets `redis://redis:6379`).
- Quality gate fails sources that match a hardcoded-secret pattern.
- Design RAG and Kotlin RAG refuse empty corpora instead of inventing content.

## AI assist

`libs/shared` ships `PortfolioAiClient`. Without `OPENAI_API_KEY` (and without `SPRING_AI_MODEL_CHAT=openai`), every assist call returns a deterministic `offline-...` string. A live call waits at most 20 seconds and returns that same string if the model times out or throws. Tests assert the offline path and that fallback. They do not call OpenAI.

## Build and test

```bash
./mvnw -B test
```

Maven tests use H2 for P2/P8. They do not require Postgres, Redis, Docker, or an OpenAI key. The default path is offline.

## Still open

Close-out date: 2026-10-07. The code in this repo is finished.

`./mvnw -B test` on 2026-10-07, finished 2026-10-07T04:34:35+02:00: BUILD SUCCESS, 123 tests, 0 failures, 0 errors, 0 skipped. Maven total time 41.967 s.

These are the only leftovers. They need you, not more code.

- Live OpenAI call. Checked 2026-10-07: `OPENAI_API_KEY` is absent from the process environment, the login shell, and `infra/compose/.env`. There is no root `.env`. No call was made.
- Image registry push. Checked 2026-10-07: no docker login (`~/.docker/config.json` has `credsStore=desktop` and an empty `auths` map; `docker-credential-desktop list` is empty). The local gateway image was not pushed. No cluster was created.

## License

Apache License 2.0. See [LICENSE](./LICENSE).
