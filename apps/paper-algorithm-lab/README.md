# Paper to algorithm lab

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P8** · internal port `8088` · H2 paper store + offline synthesize.

## Use cases

- Ingest a paper then synthesize a topic
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Paper to algorithm lab
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "paper-algorithm-lab", "Spring Boot", "H2 paper store + offline synthesize")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/papers|synthesize")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
# Primary path: POST /api/v1/papers|synthesize
# See PRODUCTION-SANDBOX.md for a worked example body.
```

Standalone:

```bash
export $(grep -v '^#' apps/paper-algorithm-lab/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/paper-algorithm-lab -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/paper-algorithm-lab -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
