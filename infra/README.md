# Infra

Ops topology for the live `apps/*` products. Application code stays under `apps/`.

| Path | Role |
|---|---|
| `compose/` | Docker Compose (prod image build, dev jar-mount override, optional Postgres/TLS) |
| `docker/` | Parameterized `Dockerfile.app` (repo-root build context + `JAR_FILE` arg) |
| `helm/` | Kubernetes chart for the same services |
| `monitoring/` | Prometheus and Grafana configs for compose DNS names |
| `scripts/` | Dev TLS cert helper (`generate-dev-certs.sh`) |
| `certs/` | Gitignored PEM output for the `tls` compose profile |

## Compose profiles and files

| Mode | Command |
|---|---|
| Prod images (H2 in P2/P8) | `docker compose -f docker-compose.yml --env-file .env up -d --build` |
| Prod + Postgres (P2/P8 `pgvector`) | `docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile prod --env-file .env up -d --build` |
| Dev jar mounts | `docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env up -d` |
| TLS on `:8443` | Run `./infra/scripts/generate-dev-certs.sh`, then add `--profile tls` |

Build jars first from the repo root: `./mvnw -DskipTests package`.

`docker compose config` may warn about missing jar paths until `package` completes; that is expected.

### Environment

Copy `compose/.env.example` to `compose/.env` (gitignored). Required: `GATEWAY_SECURITY_APIKEY`, `GRAFANA_ADMIN_PASSWORD`. No demo defaults for those.

- `REDIS_URL` defaults to `redis://redis:6379` in compose for the gateway (distributed rate limiting when wired; in-process bucket today).
- `OIDC_ISSUER_URI` enables optional OIDC resource-server checks on the gateway when set.
- `OPENAI_API_KEY` plus `SPRING_AI_MODEL_CHAT=openai` enable Spring AI `ChatClient` via `PortfolioAiClient`; otherwise apps stay offline-first.
- Postgres JDBC for P2/P8: use `docker-compose.prod.yml` and profile `prod`, or set `DESIGN_RAG_*` / `PAPER_LAB_*` vars from `.env.example`.

`./mvnw test` uses in-memory H2 for unit tests; Postgres is not required for CI.

Only `ai-edge-gateway` publishes host port `8080` (plain HTTP). Optional `tls-proxy` terminates TLS on `8443` and forwards to the gateway.

## Helm

```bash
helm template portfolio infra/helm/agentic-ai-portfolio \
  --set gatewaySecurityApiKey=local-only-not-for-prod
```

Images are expected as `agentic-ai/<app>:1.0.0-SNAPSHOT`. Build and push separately; the chart does not compile from source.
