# Security checklist — mcp-broker

Date: 2026-09-23

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys | PASS | `MCP_BROKER_API_KEY` / `app.security.api-key` |
| `.env` gitignored; `.env.example` only | PASS | |
| No private keys | PASS | |
| Auth | PASS | 401 missing/wrong; actuator open |
| Bypass ey* / valid-test-token | PASS | |
| Health no skill brands | PASS | |

```text
mvn -pl apps/mcp-broker -am test
```

## Tests run

```text
mvn -pl apps/mcp-broker -am test
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
