# Source quality gate

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P3** · internal port `8083` · Offline PASS/FAIL scan including secret patterns.

## Use cases

- Gate a Java source snippet
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Source quality gate
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "quality-gate", "Spring Boot", "Offline PASS/FAIL scan including secret patterns")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/gate")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/gate" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/quality-gate/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/quality-gate -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/quality-gate -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
