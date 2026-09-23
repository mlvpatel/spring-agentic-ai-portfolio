# Stress results

Date: 2026-09-24

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

- `/api/v1/generate` and `/api/v1/ingest` remain ambiguous across apps; use `/svc/<app>/...` for those.
- No cloud deploy in this workspace.

