# Security checklist — security-review-assistant

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys | PASS | `SECURITY_REVIEW_API_KEY` / test fixture only |
| Auth on `/api/**` | PASS | X-API-Key / Bearer equals configured key |
| Reject ey* / valid-test-token bypass | PASS | Covered by tests |
| Health has no skill brands | PASS | |

```bash
mvn -pl apps/security-review-assistant -am test
```
