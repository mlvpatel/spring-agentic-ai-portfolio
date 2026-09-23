# Security checklist — ai-validated-integration-harness

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys | PASS | `AI_HARNESS_API_KEY` / test fixture only |
| Auth on `/api/**` | PASS | X-API-Key / Bearer equals configured key |
| Reject ey* / valid-test-token bypass | PASS | Covered by tests |
| Health has no skill brands | PASS | |

```bash
mvn -pl apps/ai-validated-integration-harness -am test
```
