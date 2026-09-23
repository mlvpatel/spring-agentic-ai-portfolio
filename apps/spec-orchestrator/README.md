# Spec run orchestrator

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P4** · internal port `8084` · Offline COMPLETED runs from a brief.

## Use cases

- Create a run with constitution/spec/tasks artifacts
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Spec run orchestrator
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "spec-orchestrator", "Spring Boot", "Offline COMPLETED runs from a brief")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/runs")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/runs" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/spec-orchestrator/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/spec-orchestrator -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/spec-orchestrator -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
