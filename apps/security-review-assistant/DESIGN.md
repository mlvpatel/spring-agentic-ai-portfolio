# Authorized security review assistant design

S4 Authorized Security Review Assistant: scoped checklist review (no offensive runtime).

## Stack pin

Java 21 · Boot 4.1.1 · Spring AI BOM 2.0.1 (unused; offline path) · WebMVC.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/actuator/health` | no | Readiness |
| POST | `/api/v1/review` | yes | Primary offline flow |

## Out of scope

- Paid OpenAI/Anthropic calls in default tests
- Cloud production deploy
