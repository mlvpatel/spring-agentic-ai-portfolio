# Multimodal support desk design

S1 Multimodal Support Desk: ticket + optional image meta → offline triage.

## Stack pin

Java 21 · Boot 4.1.1 · Spring AI BOM 2.0.1 (unused; offline path) · WebMVC.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/actuator/health` | no | Readiness |
| POST | `/api/v1/triage` | yes | Primary offline flow |

## Out of scope

- Paid OpenAI/Anthropic calls in default tests
- Cloud production deploy
