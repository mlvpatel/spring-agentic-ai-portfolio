# Stress results

## 2026-10-03 sample

Laptop Docker Desktop sample against `ai-edge-gateway` on host port 8080. Started 2026-10-03 00:24 +0200 (`2026-10-02T22:24Z`).
Method: `scripts/run-stress-accuracy.sh`, 50 concurrent `curl` requests (`xargs -P 50`). API key from gitignored `infra/compose/.env` (not printed). Not a certified benchmark.
Stack: `docker compose -f infra/compose/docker-compose.yml --env-file .env up -d --build`, then the script, then `docker compose down`.

Accuracy is the script's single checked request. The success columns are the 50-way burst.

| app | accuracy | auth missing/ey*/valid-test-token | success | success rate | errors | p50 ms | p95 ms | happy HTTP |
|---|---|---|---|---|---|---|---|---|
| `yagni-copilot` | PASS | 401/401/401 | 49/50 | 98% | 1/50 | 44 | 94 | 200 |
| `design-rag-studio` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 39 | 134 | 200 |
| `design-rag-empty` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 26 | 33 | 200 |
| `quality-gate` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 17 | 23 | 200 |
| `quality-gate-secret` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 22 | 31 | 200 |
| `spec-orchestrator` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 31 | 49 | 200 |
| `agent-observability` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 19 | 32 | 200 |
| `mcp-broker` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 37 | 49 | 200 |
| `app-factory` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 32 | 44 | 200 |
| `paper-algorithm-lab` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 52 | 78 | 200 |
| `system-architect` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 39 | 51 | 200 |
| `ai-edge-gateway` | PASS | 401/401/401 | 49/50 | 98% | 1/50 | 35 | 48 | 200 |
| `multimodal-support-desk` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 42 | 65 | 200 |
| `kotlin-rag-empty` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 64 | 98 | 200 |
| `kotlin-rag-microservice` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 35 | 59 | 200 |
| `ai-validated-integration-harness` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 19 | 35 | 200 |
| `security-review-assistant` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 32 | 50 | 200 |

The two errors were HTTP 500 on `POST /api/v1/patch` during the burst (`{"status":500,"error":"Internal Server Error","path":"/api/v1/patch"}`), one in the yagni-copilot case at `2026-10-02T22:24:29.989Z` and one in the gateway case at `2026-10-02T22:26:21.783Z`. Each case's accuracy request was HTTP 200. The other fifteen routes were 50/50.

## 2026-10-03 patch rerun

After `YagniPatchService` stopped sharing one `JavaParser`. Docker Desktop was up. Started only `redis`, `yagni-copilot`, and `ai-edge-gateway` (`docker compose -f infra/compose/docker-compose.yml --env-file .env up -d --build redis yagni-copilot`, then the gateway with `--no-deps`). The other apps were not rebuilt. Method: 50 concurrent `curl` requests (`xargs -P 50`) to `POST /api/v1/patch` on `localhost:8080`, same body as the yagni row above. API key from gitignored `infra/compose/.env` (not printed). Recorded at `2026-10-02T22:51:57Z` (2026-10-03 00:51 +0200). Stack brought down afterward. Not a certified benchmark. The full-sample table above is unchanged.

| check | result |
|---|---|
| accuracy | HTTP 200, `mode` offline |
| auth missing/ey*/valid-test-token | 401/401/401 |
| burst | 50/50 HTTP 200 |
| errors | 0/50 |
| p50 ms | 34 |
| p95 ms | 50 |

## 2026-09-29 attempt

Docker daemon was reachable (`docker info` succeeded). No compose services were running (`docker compose ps` empty; `localhost:8080` not answering). Skipped inventing latency. Prior 2026-09-24 sample below is unchanged. Re-run `scripts/run-stress-accuracy.sh` after `docker compose -f infra/compose/docker-compose.yml --env-file .env up -d --build`.


## 2026-09-28 attempt

Docker daemon was not running (`docker info` could not reach the socket). No new latency numbers. Prior sample below is unchanged from 2026-09-24.

## 2026-09-24 sample

Laptop Docker Desktop sample against `ai-edge-gateway` on host port 8080.
Method: 50 concurrent `curl` requests via `xargs -P 50` (hey/wrk not installed).
API key from gitignored `infra/compose/.env` only (not printed here).
Not a certified benchmark.

| app | accuracy | auth missing/ey*/valid-test-token | success | success rate | errors | p50 ms | p95 ms | happy HTTP |
|---|---|---|---|---|---|---|---|---|
| `yagni-copilot` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 12 | 17 | 200 |
| `design-rag-studio` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 19 | 30 | 200 |
| `design-rag-empty` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 23 | 200 |
| `quality-gate` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 12 | 18 | 200 |
| `quality-gate-secret` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 12 | 16 | 200 |
| `spec-orchestrator` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 19 | 30 | 200 |
| `agent-observability` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 12 | 22 | 200 |
| `mcp-broker` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 14 | 19 | 200 |
| `app-factory` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 20 | 27 | 200 |
| `paper-algorithm-lab` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 22 | 200 |
| `system-architect` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 17 | 30 | 200 |
| `ai-edge-gateway` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 24 | 200 |
| `multimodal-support-desk` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 14 | 21 | 200 |
| `kotlin-rag-empty` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 17 | 23 | 200 |
| `kotlin-rag-microservice` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 23 | 200 |
| `ai-validated-integration-harness` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 33 | 200 |
| `security-review-assistant` | PASS | 401/401/401 | 50/50 | 100% | 0/50 | 15 | 21 | 200 |

## Gaps found and fixes

1. Gateway only forwarded to one `GATEWAY_DOWNSTREAM_URL`. Added unique path aliases and `/svc/<app>/**` prefixes with rewrite.
2. Stress retarget failed because shell-exported `GATEWAY_DOWNSTREAM_URL` overrode the compose `.env` file. Harness now exports the new URL on retarget.
3. Kotlin RAG empty corpus threw instead of a structured refuse. Now returns `refused: true` with empty hits (HTTP 200), matching design-rag.

## Still open

- Generate and ingest use unique gateway paths: `/api/v1/design-rag/*`, `/api/v1/app-factory/generate`, `/api/v1/kotlin-rag/ingest`, or full app APIs under `/svc/<app>/...`.
- No cloud deploy in this workspace.

