# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://multimodal-support-desk:8091`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Ticket triage

## Use case

Offline high-severity triage with image meta

## Request / response summary

- Method and path: `POST /api/v1/triage` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'mode':'offline','severity':'high','hasImageMeta':true,'summary':'Triage: Payment outage in checkout','nextSteps':['ack customer','inspect attached image meta']}`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Deterministic offline or local path; no OpenAI key used.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 6 |
| avg | 7.0 |
| max | 8 |
| samples | 7 8 6 6 8 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 14 ms |
| p95 | 21 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
