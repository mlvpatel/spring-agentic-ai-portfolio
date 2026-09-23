# Security review assistant

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **S4** · internal port `8094` · Authorized checklist review only.

## Use cases

- Review an auth artefact
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Security review assistant
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "security-review-assistant", "Spring Boot", "Authorized checklist review only")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/review")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/review" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/security-review-assistant/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/security-review-assistant -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/security-review-assistant -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
