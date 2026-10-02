# Requirements: Spring agentic AI portfolio

**Defined:** 2026-10-03
**Core Value:** A keyed request to a live app returns a real offline answer, and the gateway can send that request to the right app.

## v1 Requirements

Requirements the reactor already meets. Each one has a test named in phase 1 verification.

### Authentication

- [x] **AUTH-01**: A request with no API key is rejected, and a Bearer token of `ey*` or `valid-test-token` is rejected when that string is not the configured key.

### Patch

- [x] **PATCH-01**: `POST /api/v1/patch` with the configured key and no OpenAI key returns HTTP 200, `mode` `offline`, and a patch string.
- [x] **PATCH-02**: Many threads calling `YagniPatchService.propose` on one instance do not throw, and each result matches the cyclomatic complexity and AST depth of a single-threaded call on the same source.

### Gateway

- [x] **GATE-01**: The path `/api/v1/patch` resolves to the yagni-copilot downstream.

### Embeddings

- [x] **EMBED-01**: `PortfolioEmbeddingClient.embed` returns the `HashingTextEmbedder` vector when the API key is blank or no `EmbeddingModel` bean is present, and does not call the model.

## v2 Requirements

None. Work that is not in the reactor is listed as out of scope, not as a future requirement.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Cloud cluster deploy | No cluster apply or registry publish in this tree |
| Certified latency benchmark | Laptop curl samples in `docs/stress-results.md` are checks, not an SLO |
| Live OpenAI calls in `./mvnw -B test` | Tests keep the model at `none` |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 | Phase 1 | Satisfied |
| PATCH-01 | Phase 1 | Satisfied |
| PATCH-02 | Phase 1 | Satisfied |
| GATE-01 | Phase 1 | Satisfied |
| EMBED-01 | Phase 1 | Satisfied |

**Coverage:**
- v1 requirements: 5 total
- Mapped to phases: 5
- Unmapped: 0

---
*Requirements defined: 2026-10-03*
*Last updated: 2026-10-03 after import from the live tests*
