# Security checklist — design-rag-studio

Date: 2026-09-23

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys or tokens in source | PASS | Key from `app.security.api-key` / `DESIGN_RAG_API_KEY`; empty key fails startup |
| `.env` gitignored; only `.env.example` in tree | PASS | Empty placeholder |
| No private `.p12` / `.pem` committed | PASS | None |
| Auth / input handling | PASS | Missing/wrong key → 401; actuator exempt |
| Auth bypass strings rejected | PASS | Tests reject Bearer `ey*` and `valid-test-token` |
| Prompt-injection / tool-abuse | N/A | Offline/deterministic default |
| Health has no skill/plugin brands | PASS | Asserted in unit test |

## Tests run

```text
mvn -pl apps/design-rag-studio -am test
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
