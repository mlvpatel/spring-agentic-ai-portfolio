# App blueprint factory

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P7** · internal port `8087` · Deterministic scaffold files.

## Use cases

- Generate a crud-api blueprint
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title App blueprint factory
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "app-factory", "Spring Boot", "Deterministic scaffold files")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /svc/app-factory/api/v1/generate")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//svc/app-factory/api/v1/generate" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/app-factory/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/app-factory -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/app-factory -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
