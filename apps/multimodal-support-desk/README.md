# Multimodal support desk

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **S1** · internal port `8091` · Offline triage with image meta.

## Use cases

- Triage a support ticket
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Multimodal support desk
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "multimodal-support-desk", "Spring Boot", "Offline triage with image meta")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/triage")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/triage" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/multimodal-support-desk/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/multimodal-support-desk -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/multimodal-support-desk -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
