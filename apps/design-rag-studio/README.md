# Design token RAG

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](../../LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](../../pom.xml)

Catalog **P2** · internal port `8082` · JDBC/H2 token corpus; refuse if empty.


## Retrieval

Default profile uses H2 + SQL `LIKE`. With Spring profile `pgvector` (or `design.rag.vector-retrieval=true`),
ingest also fills an in-memory hashing-embedding index and `retrieve` ranks by real cosine similarity.
Compose `--profile pgvector` (alias of `postgres`) starts Postgres for JDBC; unit tests do not need Docker for the vector path.

## Use cases

- Ingest CSV tokens then generate a stack snippet
- Reject missing API key, `Bearer ey*`, and `Bearer valid-test-token` (expect 401)

## Container view

See the portfolio [C4 diagrams in the root README](../../README.md#architecture-c4).

## Run

Live LLM calls use shared `PortfolioAiClient` (offline by default; Spring AI ChatClient only with `OPENAI_API_KEY` and `SPRING_AI_MODEL_CHAT=openai`).

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
