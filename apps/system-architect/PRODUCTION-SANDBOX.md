# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://system-architect:8089`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Architecture ADR

## Use case

Produce ADR and C4 mermaid offline

## Request / response summary

- Method and path: `POST /api/v1/architect` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'c4Mermaid':'C4Context\ntitle System context\nPerson(user, \'User\')\nSystem(sys, \'Proposed system\', \'Build checkout API for EU retail\')\nRel(user, sys, \'Uses\')\n','requirements':'Build checkout API for EU retail','mode':'offline','capacity':{'notes':'Capacity: estimate RPS * p99 latency headroom before sharding.','assumptions':'100 RPS peak, 200ms p99 budget','instances':2},'parallelReview`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Deterministic offline or local path; no OpenAI key used.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 7 |
| avg | 7.2 |
| max | 8 |
| samples | 7 7 7 8 7 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 17 ms |
| p95 | 30 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
