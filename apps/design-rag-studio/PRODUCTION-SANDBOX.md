# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://design-rag-studio:8082`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Design token RAG generate

## Use case

Ingest CSV tokens then generate stack snippet

## Request / response summary

- Method and path: `POST /api/v1/generate` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'refused':false,'a11y':['color-contrast-from-tokens','focus-ring','label-required'],'layout':{'spacing':'token-driven','type':'single-column'},'tokens':{'color.primary':{'notes':'brand','value':'#0B1F33'}},'stack':'react','mode':'jdbc-h2','intent':'primary'}`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Response mode was jdbc-h2 (local H2), not a live LLM path.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 6 |
| avg | 6.8 |
| max | 7 |
| samples | 6 7 7 7 7 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 19 ms |
| p95 | 30 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
