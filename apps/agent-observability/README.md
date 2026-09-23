# Trajectory observability

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P5** · internal port `8085` · Cluster failed tool calls offline.

## Use cases

- Record then analyze trajectories
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

```mermaid
C4Container
  title Trajectory observability
  Person(user, "Caller")
  Container(gw, "ai-edge-gateway", "Gateway", "Auth + route")
  Container(app, "agent-observability", "Spring Boot", "Cluster failed tool calls offline")
  Rel(user, gw, "X-API-Key")
  Rel(gw, app, "POST /api/v1/trajectories|analyze")
```

## Run

Through the compose gateway (preferred):

```bash
# after infra/compose is up with env file keys set
# Primary path: POST /api/v1/trajectories|analyze
# See PRODUCTION-SANDBOX.md for a worked example body.
```

Standalone:

```bash
export $(grep -v '^#' apps/agent-observability/.env.example 2>/dev/null | xargs)  # or set the app API key env
./mvnw -pl apps/agent-observability -am spring-boot:run
```

## Test

```bash
./mvnw -pl apps/agent-observability -am test
```

## License

Apache-2.0. See the repository [LICENSE](../../LICENSE).
