# Sandbox results

Date: 2026-09-24

Docker Desktop compose project `compose` from `infra/compose` with gitignored `infra/compose/.env`.
All feature calls went through `ai-edge-gateway` on host port 8080. Latency is a laptop sample of five sequential requests, not a load test. No real OpenAI keys were used.

| project | result | happy HTTP | auth (missing / ey* / valid-test-token) | min / avg / max ms | path |
|---|---|---|---|---|---|
| `yagni-copilot` | pass | 200 | 401 / 401 / 401 | 9 / 9.8 / 11 | `POST /api/v1/patch` |
| `design-rag-studio` | pass | 200 | 401 / 401 / 401 | 6 / 6.8 / 7 | `POST /api/v1/generate` |
| `quality-gate` | pass | 200 | 401 / 401 / 401 | 7 / 7.2 / 8 | `POST /api/v1/gate` |
| `spec-orchestrator` | pass | 200 | 401 / 401 / 401 | 6 / 6.8 / 8 | `POST /api/v1/runs` |
| `agent-observability` | pass | 200 | 401 / 401 / 401 | 6 / 7.0 / 8 | `POST /api/v1/analyze` |
| `mcp-broker` | pass | 200 | 401 / 401 / 401 | 7 / 7.2 / 8 | `POST /api/v1/invoke` |
| `app-factory` | pass | 200 | 401 / 401 / 401 | 6 / 6.6 / 7 | `POST /api/v1/generate` |
| `paper-algorithm-lab` | pass | 200 | 401 / 401 / 401 | 7 / 7.0 / 7 | `POST /api/v1/synthesize` |
| `system-architect` | pass | 200 | 401 / 401 / 401 | 7 / 7.2 / 8 | `POST /api/v1/architect` |
| `ai-edge-gateway` | pass | 200 | 401 / 401 / 401 | 7 / 7.4 / 9 | `POST /api/v1/patch` |
| `multimodal-support-desk` | pass | 200 | 401 / 401 / 401 | 6 / 7.0 / 8 | `POST /api/v1/triage` |
| `kotlin-rag-microservice` | pass | 200 | 401 / 401 / 401 | 6 / 6.6 / 7 | `POST /api/v1/query` |
| `ai-validated-integration-harness` | pass | 200 | 401 / 401 / 401 | 8 / 8.2 / 9 | `POST /api/v1/validate` |
| `security-review-assistant` | pass | 200 | 401 / 401 / 401 | 7 / 7.0 / 7 | `POST /api/v1/review` |

Totals: 14 pass, 0 fail, 14 projects.

## Fixes applied during sandbox bring-up

1. Grafana volume: nested bind under a read-only provisioning mount failed on Docker Desktop. Compose now binds `dashboards.yml` as a file and JSON dashboards at `/var/lib/grafana/dashboards`; provider path updated to match.
2. Gateway routes: Spring Cloud Gateway 5 binds under `spring.cloud.gateway.server.webflux`. Updated `apps/ai-edge-gateway/src/main/resources/application.yml` so `/api/**` proxies again.

## Stop the stack

```bash
cd infra/compose && docker compose down
```

Leave running if you still need host :8080 / :9090 / :3000.

