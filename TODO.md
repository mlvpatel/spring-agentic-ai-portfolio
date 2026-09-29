# Implementation plan

Plan of record for this workspace. Status date: **2026-09-29**.

The canvas at `~/.cursor/projects/Users-mlvpatel-Downloads-java-AI/canvases/portfolio-implementation-plan.canvas.tsx` is a 2026-09-21 audit of the old numbered `01–10` modules (Boot 3.3.3). It is not current. Use this file. Canvas now shows a historical banner pointing here.

---

## Done (verified 2026-09-29)

Verified in the local tree and on GitHub mlvpatel/spring-agentic-ai-portfolio at public tip **`ae0171f`** (`ae0171f42e975ef404ebe90a1b13cf1535a11beb`).

1. Live reactor: `libs/shared` plus apps P1–P10 and stretch S1–S4 under `apps/` (14 app modules in root `pom.xml`).
2. Parent BOM: Spring Boot **4.1.1**, Spring AI **2.0.1**, Spring Cloud **2025.1.3**, Java 21.
3. `PortfolioAiClient` in `libs/shared` (offline default; live path needs `OPENAI_API_KEY` and ChatModel). P1 surfaces assist text in `PatchResponse.explanation` and `mode` follows `isLive()`.
4. Edge gateway (`apps/ai-edge-gateway`): unique `/svc/<app>/**` routes; remaps downstream API keys; `REDIS_URL` from default Compose redis.
5. Rate limit: `RedisRateLimitBucketStore` when `REDIS_URL` is set; in-memory store for tests.
6. Compose default file builds via `infra/docker/Dockerfile.app`. Jar mounts stay in `docker-compose.dev.yml`. Redis starts without a special profile. Postgres via `--profile postgres|pgvector|prod`.
7. Optional OIDC: `OIDC_ISSUER_URI` / `gateway.security.oidcIssuerUri` on the gateway.
8. CI: `.github/workflows/ci.yml` runs `./mvnw -B -DskipITs verify` + gitleaks. `.gitleaks.toml` allowlists only `TODO.md`.
9. Community docs: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, issue/PR templates.
10. Unit tests: **`./mvnw -B test` → BUILD SUCCESS, 115 tests, 0 failures** (2026-09-29).
11. Helm: gateway Deployment injects downstream `*_API_KEY` env names and `REDIS_URL`. `helm template` dry-run succeeds.
12. Docs BOM pin: Boot 4.1.1 / Spring AI 2.0.1 in design docs.
13. Review fixes + ponytail shrink: shared `ApiKeyAuthFilter` / `ApiKeyProperties` / auto-config; `RemoteToolClient` timeouts; `GatewayFilterStartupValidator`; `DownstreamCredentialMapper` fallbacks; kotlin-rag `valid-test-token` rejection; `ApiKeyAuthFilterTest`.
14. P2 vector retrieval: under `pgvector` (or `design.rag.vector-retrieval=true`), hashing embeddings + real cosine via `InMemoryCosineVectorStore`. Empty corpus refuses; matching snippet returned (unit + Spring tests). Compose `--profile pgvector` starts Postgres for JDBC.
15. P8 kotlin-rag: same cosine path behind `kotlin.rag.vector-retrieval` / `pgvector`.

Public publish tip: **`ae0171f`** (parent `a43f8e4`). CI on older `67494a0`: [36391907928](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/36391907928) success.

---

## Pending / skipped

1. Full Docker stress re-run against a live compose stack: daemon was up on 2026-09-29, but no containers were running at verify time. Numbers in `docs/stress-results.md` remain the 2026-09-24 sample unless a new sample is appended after `docker compose up`. Do not invent latency.
2. True pgvector Postgres + Spring AI EmbeddingModel / Testcontainers path: skipped as heavy; in-memory cosine store covers retrieval tests without Docker. JDBC Postgres still available under `--profile pgvector`.

Out of scope: cloud cluster deploy, image registry publish, production Kubernetes.

---

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history. Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Commits go on top of public main; no force-push. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, `.planning/`, or `archive/`.

---

## Blocked

None for the open-source goal.

Notes (not blockers):

- Older run [35934273926](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/35934273926) failed on private-repo Actions billing. The repo is public now.
- Cloud/cluster production deploy is intentionally dropped.

---

## How to read this repo

| Layer | Meaning | Source of truth |
|---|---|---|
| L0 Platform | Runtime every Java app builds on | Spring AI + Boot parent BOM |
| L1 Dev harness | How agents build and review | local skills / plugins (not required in the published tree) |
| L2 Portfolio products | Catalog apps P1–P10 + S1–S4 | `apps/<kebab-name>/` |

**Archive:** `archive/legacy-portfolio/` is reference only (local; not published).

**Live reactor:** `libs/shared` + P1–P10 + S1–S4.

---

## Rules

- Product `/health` never lists skill or plugin brand names.
- Maturity labels for live apps: Scaffold | Engine-ready | AI-wired | Prod-blocked.
- Do not claim `ChatClient` runs when the API key is absent.
- Do not mark cloud or production ready from offline unit tests alone.

---

## Verification

```bash
./mvnw -B test
# Load compose secrets from a gitignored env file (do not inline real keys here):
docker compose -f infra/compose/docker-compose.yml --env-file /path/to/.env config
helm template portfolio infra/helm/agentic-ai-portfolio --set gatewaySecurityApiKey=local-dry-run
```
