# Design-system RAG studio design

P2 — Ingest design CSV tokens into a JDBC corpus (H2 by default) and generate stack-specific UI specs grounded in those tokens.

## Live backends

- Default: embedded H2 JDBC (`mode=jdbc-h2`). No paid API key.
- Optional profile `pgvector`: Postgres via compose service `design-rag-db` or Testcontainers IT (`JdbcDesignCorpusIT`, skipped without Docker).
- OpenAI embeddings/chat stay behind env vars and are not wired in default tests.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/actuator/health` | no | Readiness |
| POST | `/api/v1/ingest` | yes | CSV token ingest |
| POST | `/api/v1/generate` | yes | Grounded UI spec |
