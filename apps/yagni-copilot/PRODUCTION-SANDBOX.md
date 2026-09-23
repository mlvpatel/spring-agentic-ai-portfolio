# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://yagni-copilot:8081`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

YAGNI patch proposal

## Use case

Offline patch with complexity bounds

## Request / response summary

- Method and path: `POST /api/v1/patch` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'mode':'offline','changeRequest':'rename method','patch':'// YAGNI patch for: rename method\n// Apply the smallest edit that satisfies the request.\n// Keep using existing types; no new Factory/Builder/Strategy unless required.\npublic class Demo { public int add(int a, int b) { return a + b; } }\n','cyclomaticComplexity':1,'astDepth':7,'maxCyclomatic':5,'withinBounds':true,'notes':['Within YAGNI`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Deterministic offline or local path; no OpenAI key used.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 9 |
| avg | 9.8 |
| max | 11 |
| samples | 11 9 11 9 9 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 12 ms |
| p95 | 17 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
