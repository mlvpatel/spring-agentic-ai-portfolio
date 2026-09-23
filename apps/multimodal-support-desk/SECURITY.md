# Security checklist — multimodal-support-desk

| Check | Result | Notes |
|---|---|---|
| No hardcoded API keys | PASS | `MULTIMODAL_DESK_API_KEY` / test fixture only |
| Auth on `/api/**` | PASS | X-API-Key / Bearer equals configured key |
| Reject ey* / valid-test-token bypass | PASS | Covered by tests |
| Health has no skill brands | PASS | |

```bash
mvn -pl apps/multimodal-support-desk -am test
```
