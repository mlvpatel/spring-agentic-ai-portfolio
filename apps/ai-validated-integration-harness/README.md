# AI validated integration harness

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **S3** · internal port `8093` · Offline contract vs observed.

## Use cases

- Validate an integration payload
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

See the portfolio [C4 diagrams in the root README](../../README.md#architecture-c4).

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
curl -sS -X POST "http://localhost:8080//api/v1/validate" \
  -H "Content-Type: application/json" -H "X-API-Key: $GATEWAY_SECURITY_APIKEY" \
  -d '{}'
```

Standalone:

```bash
export $(grep -v '^#' apps/ai-validated-integration-harness/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/ai-validated-integration-harness -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/ai-validated-integration-harness -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
