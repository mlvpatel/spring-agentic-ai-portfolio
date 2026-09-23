# Infra

Ops topology for the live `apps/*` products. Product logic stays in `apps/`.

| Path | Role |
|---|---|
| `compose/` | Docker Compose stack for P1–P10 jars under `apps/` |
| `helm/` | Chart for the same services (images named after each app) |
| `monitoring/` | Prometheus / Grafana configs pointed at `apps/*` DNS names |
| `scripts/` | Placeholder for cert/secret helpers (repo also keeps `scripts/` at root) |

## Compose

```bash
mvn -DskipTests package
cp infra/compose/.env.example infra/compose/.env   # set GATEWAY_SECURITY_APIKEY + GRAFANA_ADMIN_PASSWORD
GATEWAY_SECURITY_APIKEY=… GRAFANA_ADMIN_PASSWORD=… \
  docker compose -f infra/compose/docker-compose.yml --env-file infra/compose/.env config
```

Only `ai-edge-gateway` publishes host port `8080`. Internal apps stay on the compose network. Gateway routes unique `/api/v1/...` aliases and `/svc/<app>/**` prefixes. `GATEWAY_DOWNSTREAM_URL` remains a fallback for unmatched `/api/**` paths.

## Helm

```bash
helm template portfolio infra/helm/agentic-ai-portfolio \
  --set gatewaySecurityApiKey=local-only-not-for-prod
```

Images are expected as `agentic-ai/<app>:1.0.0-SNAPSHOT`. Build and push those images separately; this chart does not build from source.
