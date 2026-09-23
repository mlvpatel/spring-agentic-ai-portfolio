# Design token RAG

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P2** · internal port `8082` · JDBC/H2 token corpus; refuse if empty.

## Use cases

- Ingest CSV tokens then generate a stack snippet
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Design token RAG
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "design-rag-studio", "Spring Boot", "JDBC/H2 token corpus; refuse if empty")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /svc/design-rag-studio/api/v1/ingest|generate")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
# Primary path: POST /svc/design-rag-studio/api/v1/ingest|generate
# See PRODUCTION-SANDBOX.md for a worked example body.
```

Standalone:

```bash
export $(grep -v '^#' apps/design-rag-studio/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/design-rag-studio -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/design-rag-studio -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
