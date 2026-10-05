# Implementation plan

Status date: 2026-10-03. Checked again 2026-10-06: `./mvnw -B test` BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped, wall time 35.59 s.

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

The 2026-10-03 full sample is in `docs/stress-results.md`. Fifteen routes were 50/50. The yagni-copilot burst and the gateway `POST /api/v1/patch` burst each returned one HTTP 500 (49/50). Both accuracy checks were HTTP 200. The same file has a later patch-only rerun: 50/50 HTTP 200, p50 34 ms, p95 50 ms, at `2026-10-02T22:51:57Z`.

## Open items, 2026-10-03

### Patch HTTP 500

Both 49/50 rows in `docs/stress-results.md` are `POST /api/v1/patch` forwarded to `http://yagni-copilot:8081` (`scripts/run-stress-accuracy.sh` cases `yagni-copilot` and `ai-edge-gateway`). The recorded body is the servlet error JSON, so the throw is in yagni-copilot. The gateway rate limiter answers 429, and `InMemoryRateLimitBucketStore.tryConsume` is synchronized. `PortfolioAiClient.assist` on the offline path only reads fields set in the constructor. `HashingTextEmbedder` is not on this route.

Cause, confirmed in javaparser-core 3.26.1 and `YagniPatchService`: the service is a Spring singleton and its constructor keeps one `JavaParser`. `measure` calls `javaParser.parse` on every request (line 87). `JavaParser` holds one `GeneratedJavaParser astParser`. `getParserForProvider` either stores that parser or calls `reset` on it, then parse uses its token state and `problems` list. `parse` catches `Exception` and turns it into a `ParseResult`, but an exception thrown from that catch (shared `problems` list) is rethrown, and `findAll` on a torn tree throws after `parse` returns. Either one becomes HTTP 500. One failure in fifty calls matches that race.

Fix: build a new `JavaParser` with `JAVA_21` inside `measure` and delete the field. Do not catch the failure in the controller.

Proof: `YagniPatchServiceTest` runs 50 threads against one service instance and fails if any call throws or if cyclomatic complexity or AST depth disagrees with a single-threaded call on the same source. Then `./mvnw -B test`. If Docker is up, rerun only the 50-way patch burst and write the real counts in `docs/stress-results.md`. If it is down, the unit test is the proof. No invented latencies.

Result: before the fix, that test failed 1000/1000 with cyclomatic 1 against a baseline of 2 (the parse fell through to the line heuristic; the test itself did not throw). After `measure` builds a new `JavaParser` at lines 84-87, the test passes. `./mvnw -B test`: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. Docker was up. Patch burst through the gateway: 50/50 HTTP 200, 0 errors, p50 34 ms, p95 50 ms, accuracy HTTP 200 `mode` offline, auth 401/401/401.

### GSD baseline

Import from `TODO.md` and the apps that are already in the reactor. One phase, not a greenfield roadmap.

Files to add:

- `.planning/PROJECT.md`
- `.planning/REQUIREMENTS.md`
- `.planning/ROADMAP.md`
- `.planning/phases/01-shipped-portfolio/01-VERIFICATION.md`
- `.planning/phases/01-shipped-portfolio/01-01-SUMMARY.md` (the audit treats a passed verification with no summary as partial)

Done means every v1 requirement is something the code and tests already do, the phase verification cites the Maven suite and the patch concurrency test, and a fresh audit writes `passed` only if the three-source check has no unsatisfied or orphaned requirement. Cloud deploy stays out. If the audit is still `gaps_found`, leave that status and say why.

Result: those files are in `.planning/`. v1 ids are AUTH-01, PATCH-01, PATCH-02, GATE-01, EMBED-01. `init.milestone-op` reported phase_count 1, completed_phases 1. The 2026-10-03 audit was `passed` (5/5) before Nyquist was re-checked. On 2026-10-06 the audit was run again. `.planning/v1.0-MILESTONE-AUDIT.md` is `passed` (5/5) and Nyquist is `COMPLIANT` because `01-VALIDATION.md` exists.

## GSD

The 2026-09-28 audit was `gaps_found` because the planning baseline was missing. The 2026-10-06 audit in `.planning/v1.0-MILESTONE-AUDIT.md` is `passed`. Nyquist overall is `COMPLIANT`.

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

Cloud cluster deploy and image registry publish stay out.

Live OpenAI call is not done. Checked 2026-10-06: no `OPENAI_API_KEY` in the process environment, the login shell, or `infra/compose/.env`. There is no root `.env`. No live call was made.

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history. Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Commits go on top of public main; no force-push. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, or `archive/`. Product planning under `.planning/` is part of the public tree.
