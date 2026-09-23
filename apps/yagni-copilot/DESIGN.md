# YAGNI coding copilot design

P1 — REST API that turns a Java change request plus snippet into a minimal patch, bounded by cyclomatic complexity and AST depth. Deterministic offline path only.

## Goal

Accept `changeRequest` + `sourceCode`, measure complexity with JavaParser, and return a YAGNI-sized patch suggestion with pass/fail against configurable bounds.

## In scope

- Java 21 + Spring Boot 4.1.1 (parent) + Spring AI BOM 2.0.1 available but unused
- `POST /api/v1/patch` — deterministic analysis + patch text
- API key auth on `/api/**`; actuator health open
- Unit tests; `.env.example` only

## Out of scope

- Live OpenAI/Anthropic / ChatClient (offline-only; no dead AI wiring)
- SSE streaming in v1
- Redis chat memory
- Cloud deploy

## Stack pin

| Item | Choice |
|---|---|
| Java | 21 |
| Boot | 4.1.1 |
| Spring AI | BOM 2.0.1; default path does not call a model |
| Web | WebMVC + actuator + validation |
| AST | JavaParser 3.26.1 |

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/actuator/health` | no | Readiness |
| POST | `/api/v1/patch` | yes | Minimal patch + complexity gate |
