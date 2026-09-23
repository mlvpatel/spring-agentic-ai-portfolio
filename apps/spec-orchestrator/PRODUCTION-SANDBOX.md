# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://spec-orchestrator:8084`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Spec run orchestration

## Use case

Create offline COMPLETED run from brief

## Request / response summary

- Method and path: `POST /api/v1/runs` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'id':'run-1','brief':'Add refund API','status':'COMPLETED','mode':'offline','createdAt':'2026-09-23T22:24:28.617081292Z','artifacts':[{'type':'constitution','content':'Prefer small diffs and testable acceptance criteria.'},{'type':'spec','content':'Feature: Add refund API\nAcceptance: happy path + auth failure.'},{'type':'tasks','content':'1) API contract\n2) Service logic\n3) Tests'},{'type':'ro`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Deterministic offline or local path; no OpenAI key used.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 6 |
| avg | 6.8 |
| max | 8 |
| samples | 8 7 7 6 6 |

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
