# Security checklist — kotlin-rag-microservice

API key via `KOTLIN_RAG_API_KEY`. Rejects ey* / valid-test-token unless equal to configured key.

```bash
mvn -pl apps/kotlin-rag-microservice -am test
```
