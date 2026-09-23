# Security checklist — yagni-copilot

Date: 2026-09-23

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys or tokens in source | PASS | Key from `app.security.api-key` / `YAGNI_API_KEY`; empty key fails startup |
| `.env` gitignored; only `.env.example` in tree | PASS | Placeholder empty; root `.gitignore` covers `.env` |
| No private `.p12` / `.pem` committed | PASS | None in module |
| Auth / input handling | PASS | Missing/wrong key → 401; actuator exempt; validation on blank body |
| Auth bypass strings rejected | PASS | Tests reject Bearer `ey*` and `valid-test-token` |
| Prompt-injection / tool-abuse | N/A | Default mode is offline AST; no ChatClient tools |
| Health has no skill/plugin brands | PASS | Asserted in unit test |

## Tests run

```text
mvn -pl apps/yagni-copilot -am test
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
