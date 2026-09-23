# Paper algorithm lab design

P8 — Ingest paper text; synthesize a minimal Java prototype with citations.

## Live backends

- Default: H2 JDBC paper store (`mode=jdbc-h2`). No paid embeddings/LLM.
- Optional profile `pgvector`: Postgres JDBC URL via env.
- Cloud LLM calls are not wired in default tests.

## Endpoints

POST `/api/v1/papers` · POST `/api/v1/synthesize` · health
