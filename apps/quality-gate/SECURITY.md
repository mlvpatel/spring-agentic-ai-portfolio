# Security checklist — quality-gate

Date: 2026-09-23

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys | PASS | `QUALITY_GATE_API_KEY` / `app.security.api-key` |
| `.env` gitignored; `.env.example` only | PASS | |
| No private keys | PASS | |
| Auth | PASS | 401 missing/wrong; actuator open |
| Bypass ey* / valid-test-token | PASS | |
| Health no skill brands | PASS | |

```text
mvn -pl apps/quality-gate -am test
```

## Tests run

```text
mvn -pl apps/quality-gate -am test
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
