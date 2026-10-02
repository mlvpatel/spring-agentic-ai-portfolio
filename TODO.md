# Implementation plan

Status date: 2026-10-03.

The six cuts are on public main at `ecdf0ed` (`ecdf0eda002fe53d2b6c33d27255dad68894d9d1`), parent `ab6c92e`. `./mvnw -B test` on that tree: BUILD SUCCESS, 115 tests, 0 failures, 0 errors, 0 skipped. After `PortfolioEmbeddingClient`, the same command on 2026-10-03: BUILD SUCCESS, 117 tests, 0 failures, 0 errors, 0 skipped.

## Six cuts

Done in `ecdf0ed`:

1. Dropped `spring-boot-starter-validation` from the twelve live apps that never use `@Valid`. `apps/yagni-copilot/pom.xml` still has it.
2. One `ApplicationRunner` on `ApiKeySecurityAutoConfiguration` rejects a blank `app.security.api-key`. The thirteen `ApiKeyStartupValidator` copies are gone. The gateway keeps `GatewayFilterStartupValidator`.
3. `HashingTextEmbedder` lives in `libs/shared`. Design-rag and kotlin-rag call `embed` and `dotProduct`.
4. Prometheus and Grafana are gone from compose, Helm annotations, and `infra/monitoring/`.
5. Helm `hpa.yaml` and the disabled `autoscaling` blocks are gone. Each Deployment still sets `replicas`.
6. Gateway path aliases are the static CR-01 map. `gateway.downstream.keys` stays in the gateway `application.yml`.

Redis rate limiting and optional OIDC stay.

## Embeddings

Design-rag (P2) and kotlin-rag (P8) call Spring AI `EmbeddingModel` when `OPENAI_API_KEY` is set and `spring.ai.model.embedding=openai`; otherwise they keep `HashingTextEmbedder`. `mvn test` leaves the model at `none`, so it does not call the network.

## Stress

The 2026-10-03 sample is in `docs/stress-results.md`. Fifteen routes were 50/50. The yagni-copilot burst and the gateway `POST /api/v1/patch` burst each returned one HTTP 500 (49/50). Both accuracy checks were HTTP 200. Compose was brought down afterward.

## GSD

Still no roadmap; not part of this ship. `.planning/v1.0-MILESTONE-AUDIT.md` stays `gaps_found` and is not published.

## Earlier done (verified 2026-09-29)

Verified on GitHub mlvpatel/spring-agentic-ai-portfolio before the six cuts, at `ae0171f`.

1. Live reactor: `libs/shared` plus apps P1–P10 and stretch S1–S4 under `apps/` (14 app modules in root `pom.xml`).
2. Parent BOM: Spring Boot 4.1.1, Spring AI 2.0.1, Spring Cloud 2025.1.3, Java 21.
3. `PortfolioAiClient` in `libs/shared` (offline default; live path needs `OPENAI_API_KEY` and a `ChatModel`).
4. Edge gateway: unique `/svc/<app>/**` routes, downstream API key remap, `REDIS_URL` from default Compose redis.
5. Rate limit: `RedisRateLimitBucketStore` when `REDIS_URL` is set; in-memory store for tests.
6. Compose default file builds via `infra/docker/Dockerfile.app`. Postgres via `--profile postgres|pgvector|prod`.
7. Optional OIDC: `OIDC_ISSUER_URI` on the gateway.
8. CI: `./mvnw -B -DskipITs verify` plus gitleaks. `.gitleaks.toml` allowlists only `TODO.md`.
9. Community docs: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, issue and PR templates.
10. Helm: gateway Deployment injects downstream `*_API_KEY` env names and `REDIS_URL`.

CI on older `67494a0`: [36391907928](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/36391907928) success.

## Still out

Cloud cluster deploy and image registry publish stay out. The one HTTP 500 on `/api/v1/patch` under 50 concurrent calls is recorded in the stress sample, not changed here.

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history. Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Commits go on top of public main; no force-push. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, `.planning/`, or `archive/`.
