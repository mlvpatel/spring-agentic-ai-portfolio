# Production sandbox report

Date: 2026-09-24

## Sandbox

- Host: Docker Desktop on macOS (darwin)
- Compose project: `compose`
- Compose file: `infra/compose/docker-compose.yml`
- Env file: `infra/compose/.env` (gitignored; random local API key shared by gateway and apps)
- Ingress: `http://localhost:8080` (`ai-edge-gateway`)
- Downstream for this run: `http://paper-algorithm-lab:8088`
- LLM: none (sandbox key only; no real provider credentials)

## Feature tested

Paper synthesize

## Use case

Ingest paper then synthesize topic offline

## Request / response summary

- Method and path: `POST /api/v1/synthesize` through the gateway with `X-API-Key` (value redacted)
- Happy-path HTTP status: 200
- Response body (truncated, no secrets): `{'mode':'jdbc-h2','topic':'sort','java':'// Synthesized offline prototype for: sort
// Citation: Sort survey
public final class AlgorithmPrototype {
  private AlgorithmPrototype() {}
  public static int step(int x) { return x; }
}
','tests':'// assert AlgorithmPrototype.step(1) == 1;
','citations':[{'id':'paper-1','title':'Sort survey'},{'id':'paper-2','title':'Sort survey'},{'id':'paper-3'`
- Auth checks: missing key → 401; `Bearer ey*` → 401; `Bearer valid-test-token` → 401 (expect 401)
- Notes: Response mode was jdbc-h2 (local H2), not a live LLM path.

## Performance

Laptop sandbox sample (5 sequential requests via `curl -w %{time_total}`, not a load test):

| metric | ms |
|---|---|
| min | 7 |
| avg | 7.0 |
| max | 7 |
| samples | 7 7 7 7 7 |

## Stress sample

Laptop Docker sample (50 concurrent curls via xargs; not a certified benchmark):

| metric | value |
|---|---|
| success | 50/50 (100%) |
| errors | 0/50 |
| p50 | 15 ms |
| p95 | 22 ms |
| accuracy | PASS |
| auth (missing / ey* / valid-test-token) | 401/401/401 |

## Result

**pass**
