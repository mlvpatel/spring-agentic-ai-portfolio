# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://app-factory:8087`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Blueprint generate

## Use case

Generate crud-api scaffold offline

## Request / response summary

- Method and path: `POST /api/v1/generate` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'appName':'demo-api','blueprint':'crud-api','note':'Structure only; no live ChatClient generation.','files':{'pom.xml':'<project><artifactId>demo-api</artifactId><parent><artifactId>spring-boot-starter-parent</artifactId><version>3.3.3</version></parent></project>','src/main/resources/application.yml':'spring:\n  application:\n    name: demo-api\n','README.md':'# demo-api\n\nGenerated from bluepr`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Deterministic offline or local path; no OpenAI key used.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 6 |
| avg | 6.6 |
| max | 7 |
| samples | 7 7 7 6 6 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 20 ms |
| p95 | 27 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
