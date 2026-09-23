# Security checklist — ai-edge-gateway

Date: 2026-09-23

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys or tokens in source | PASS | Key from `gateway.security.apiKey` / env only; empty key fails startup |
| `.env` gitignored; only `.env.example` in tree | PASS | Root `.gitignore` covers `.env`; app has `.env.example` with empty placeholders |
| No private `.p12` / `.pem` committed | PASS | None in module |
| Auth / input handling | PASS | Missing/wrong key → 401; actuator + fallback exempt only |
| Auth bypass strings rejected | PASS | Tests reject Bearer `ey*` JWT-looking token and `valid-test-token` |
| Prompt-injection / tool-abuse | N/A | No ChatClient / model tools on the edge |
| Health has no skill/plugin brands | PASS | Asserted in unit test |

## Tests run

```text
./mvnw -pl apps/ai-edge-gateway -am test
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
