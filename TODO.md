# Implementation plan

Plan of record for this workspace. Status date: **2026-09-28**.

The canvas at `~/.cursor/projects/Users-mlvpatel-Downloads-java-AI/canvases/portfolio-implementation-plan.canvas.tsx` is a 2026-09-21 audit of the old numbered `01–10` modules (Boot 3.3.3). It is not current. Use this file. Canvas now shows a historical banner pointing here.

---

## Done (verified 2026-09-28)

Verified in the local tree and/or on GitHub.

1. Live reactor: `libs/shared` plus apps P1–P10 and stretch S1–S4 under `apps/` (14 app modules in root `pom.xml`).
2. Parent BOM: Spring Boot **4.1.1**, Spring AI **2.0.1**, Spring Cloud **2025.1.3**, Java 21.
3. `PortfolioAiClient` in `libs/shared` (offline default; live path needs `OPENAI_API_KEY` and `SPRING_AI_MODEL_CHAT=openai`). Call sites present in product apps including Kotlin RAG.
4. Edge gateway (`apps/ai-edge-gateway`): unique `/svc/<app>/**` routes and path aliases; no catch-all `/api/**`.
5. Rate limit: `RedisRateLimitBucketStore` when `REDIS_URL` is set; in-memory store for tests.
6. Compose: Postgres via `--profile postgres` / `prod`; optional TLS nginx on 8443; `infra/docker/Dockerfile.app` present.
7. Optional OIDC: `OIDC_ISSUER_URI` / `gateway.security.oidcIssuerUri` on the gateway.
8. CI workflow at `.github/workflows/ci.yml` (JDK 21, secret scan with root `*.md` exclusion, `./mvnw test`, gitleaks).
9. Community docs in tree: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `.github/ISSUE_TEMPLATE/*`, `.github/PULL_REQUEST_TEMPLATE.md` (from public `b964060`).
10. Unit tests: **`./mvnw -B test` → BUILD SUCCESS, 92 tests, 0 failures** (2026-09-28).
11. Helm: ConfigMap + gateway Deployment inject all `GATEWAY_URL_*` service URLs and optional `REDIS_URL`. Stretch apps (multimodal, kotlin-rag, harness, security-review) added to values. `helm template portfolio infra/helm/agentic-ai-portfolio --set gatewaySecurityApiKey=local-dry-run` succeeds.
12. Docs BOM pin: `docs/multiagent-sdlc-loop.md` and live DESIGN.md stack notes updated to Boot 4.1.1 / Spring AI 2.0.1.
13. Canvas historical banner added (2026-09-21; plan of record is this file).
14. Secret-scan false positive fixed: CI now excludes root `*.md` (`:**/*.md` alone missed `TODO.md`).
15. Public publish: `mlvpatel/spring-agentic-ai-portfolio` at **`67494a0`** (FF from `b964060`). CI run [`36391907928`](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/36391907928) **success** (secret scan + unit tests + gitleaks).

---

## Pending

1. Docker stress re-run: **skipped 2026-09-28** — Docker daemon down (`docker info` failed). Prior numbers remain in `docs/stress-results.md` (2026-09-24). Re-run when daemon is up; do not invent latency.

Out of scope for this plan: cloud cluster deploy, image registry publish, and production Kubernetes. Local compose and `helm template` dry-run are enough.

---

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history (`f9d069` line). Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Base was `b964060`; new commits push here without force. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, or `archive/`.

---

## Blocked

None for the open-source goal.

Notes (not blockers):

- Older run [`35934273926`](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/35934273926) failed on private-repo Actions billing. The repo is public now.
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
