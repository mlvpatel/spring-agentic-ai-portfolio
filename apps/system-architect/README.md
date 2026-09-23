# System architect

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P9** · internal port `8089` · Offline ADR + C4 Mermaid.

## Use cases

- Produce architecture artefacts from requirements
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title System architect
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "system-architect", "Spring Boot", "Offline ADR + C4 Mermaid")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/architect")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/architect" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/system-architect/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/system-architect -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/system-architect -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
