# Master roadmap and TODOs

Living backlog for the Spring AI portfolio. Counts match the tree after the 2026-09-24 production-audit gap close.

---

## Snapshot

| State | Count |
|---|---|
| Finished (this cycle) | 9 gap items closed locally |
| BLOCKED | 2 |

Finished locally: Spring AI `PortfolioAiClient`, unique gateway routes, Redis rate limit, Postgres compose profile, Dockerfiles + compose build, optional TLS, optional OIDC, docs honesty, reactor tests green.

BLOCKED: GitHub Actions billing on private repo; cloud/cluster production deploy.

---

## Finished this cycle (production audit gaps)

1. **Spring AI actually used** — `libs/shared` `PortfolioAiClient` (offline default). Apps P1–P9 + S1–S4 call `assist(...)` for one real step. Live path needs `OPENAI_API_KEY` + `SPRING_AI_MODEL_CHAT=openai`.
2. **Gateway routes** — removed catch-all `/api/**`. Unique aliases for design-rag ingest/generate, app-factory generate, kotlin-rag ingest; plus `/svc/<app>/**`.
3. **Rate limit** — Redis when `REDIS_URL` set; in-memory for unit tests. Prod compose sets Redis.
4. **Data** — H2 for `mvn test`. Compose `--profile postgres` / `prod` for P2 + P8 JDBC.
5. **Images** — `infra/docker/Dockerfile.app`; default compose builds images. `docker-compose.dev.yml` jar-mounts for fast local.
6. **TLS** — optional `--profile tls` nginx on 8443; `infra/scripts/generate-dev-certs.sh`; certs gitignored.
7. **Auth** — API keys unchanged. Optional OIDC when `OIDC_ISSUER_URI` set. Tests still 401 `ey*` / `valid-test-token`.
8. **Docs honesty** — root README, infra README, gateway DESIGN.md, stress-results note updated.
9. **Tests** — `./mvnw -B test`: **92** tests, 0 failures (2026-09-24).

---

## BLOCKED

1. **GitHub Actions billing** — workflow run `35934273926` failed on private-repo Actions minutes/billing. CI badge may stay red until billing is fixed. Do not treat as a code failure.
2. **Cloud production deploy** — no cloud account, container registry, or Kubernetes cluster target in this workspace. Local compose/helm dry-run only. No fake green cloud deploy.

---

## Follow-ups (not finished)

- Re-run full Docker stress / sandbox against rebuilt images after local daemon is free (unit tests covered auth 401; compose stack not re-hit in this cycle).
- Wire Helm values for Redis URL + unique gateway path aliases if chart drifts.
- Optional: publish images to a registry once a cloud account exists.
- Confirm Actions run after billing is restored (same workflow; do not invent a green status).

---

## How to read this repo

| Layer | Meaning | Source of truth |
|---|---|---|
| L0 Platform | Runtime every Java app builds on | Spring AI + Boot parent BOM |
| L1 Dev harness | How agents build and review | local skills / plugins (not in published tree) |
| L2 Portfolio products | Catalog apps P1–P10 + S1–S4 | `apps/<kebab-name>/` |

**Archive:** `archive/legacy-portfolio/` is reference only (kept locally; may be excluded from private publish).

**Live reactor:** `libs/shared` + P1–P10 + S1–S4.

---

## Rule (authoritative)

- Product `/health` never lists skill or plugin brand names
- Maturity for live apps: Scaffold | Engine-ready | AI-wired | Prod-blocked
- Do not claim ChatClient runs when the API key is absent
- Do not restore “Completed / Production Ready” from offline unit tests alone for cloud claims

---

## Verification

```bash
./mvnw -B test
GATEWAY_SECURITY_APIKEY=… GRAFANA_ADMIN_PASSWORD=… \
  docker compose -f infra/compose/docker-compose.yml --env-file /path/to/.env config
helm template portfolio infra/helm/agentic-ai-portfolio --set gatewaySecurityApiKey=local-dry-run
```
