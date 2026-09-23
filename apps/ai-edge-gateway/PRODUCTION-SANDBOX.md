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

Edge API key auth and proxy

## Use case

Reject bad keys; proxy authenticated patch to yagni

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
| min | 7 |
| avg | 7.4 |
| max | 9 |
| samples | 7 7 9 7 7 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 15 ms |
| p95 | 24 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
