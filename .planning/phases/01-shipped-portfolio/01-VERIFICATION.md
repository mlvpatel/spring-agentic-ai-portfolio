---
phase: 01-shipped-portfolio
verified: 2026-10-02T22:55:00Z
status: passed
score: 5/5 must-haves verified
behavior_unverified: 0
---

# Phase 1: Shipped portfolio verification report

**Phase Goal:** The live reactor answers keyed offline requests, the gateway routes `/api/v1/patch` to yagni-copilot, and concurrent patch parses do not share one `JavaParser`.
**Verified:** 2026-10-02T22:55:00Z
**Status:** passed

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | A patch request with no API key is rejected, including `ey*` and `valid-test-token` bearers that are not the configured key | VERIFIED | `ApiKeyAuthFilterTest` (missing, wrong, ey*, valid-test-token, accepts configured key). `YagniCopilotApplicationTest.patchUnauthorizedWithoutKey`, `rejectsEyJwtBypass`, `rejectsValidTestTokenBypass`. `AiEdgeGatewayApplicationTest` auth cases. |
| 2 | A keyed `POST /api/v1/patch` with no OpenAI key returns an offline patch proposal | VERIFIED | `YagniCopilotApplicationTest.patchOk` expects HTTP 200, `mode` offline, explanation prefix `offline-patch-explanation`, `withinBounds` true, patch contains `YAGNI patch`. |
| 3 | Parallel `propose` calls on one `YagniPatchService` do not throw and agree on cyclomatic complexity and AST depth | VERIFIED | `YagniPatchServiceTest.concurrentProposesDoNotThrow`: 50 threads, 20 calls each, same source as a single-threaded baseline. Before the fix this test failed (1000/1000 returned cyclomatic 1 against baseline 2). After one `JavaParser` per `measure` call it passes. |
| 4 | `/api/v1/patch` maps to yagni-copilot | VERIFIED | `DownstreamCredentialMapperTest.fallsBackWhenYamlEmpty` expects `yagni-copilot`. Gateway route `alias-patch` in `apps/ai-edge-gateway/src/main/resources/application.yml` uses `GATEWAY_URL_YAGNI`. |
| 5 | Embedding falls back to the hashing embedder when no key or embedding model is present | VERIFIED | `PortfolioEmbeddingClientTest.hashesWhenKeyOrModelMissing` compares `embed` to `HashingTextEmbedder(64)` and verifies the model is not called. `JdbcDesignCorpus` and kotlin `CorpusService` take `PortfolioEmbeddingClient`. |

**Score:** 5/5 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `apps/yagni-copilot/src/main/java/com/portfolio/yagni/service/YagniPatchService.java` | Per-request parser | EXISTS | `measure` constructs `JavaParser` at lines 84-87. No field holds a parser. |
| `apps/yagni-copilot/src/main/java/com/portfolio/yagni/web/PatchController.java` | `POST /api/v1/patch` | EXISTS | Calls `patchService.propose`. No catch that swallows errors. |
| `libs/shared/src/main/java/com/portfolio/shared/ai/PortfolioAiClient.java` | Offline assist | EXISTS | Offline `assist` returns a deterministic string and does not call `ChatClient`. |
| `apps/ai-edge-gateway/src/main/resources/application.yml` | Alias route | EXISTS | `alias-patch` path `/api/v1/patch`. |

**Artifacts:** 4/4 verified

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|----|--------|---------|
| `PatchController.patch` | `YagniPatchService.measure` | `propose` | WIRED | Controller returns `patchService.propose(request)` |
| Gateway `alias-patch` | yagni-copilot | `GATEWAY_URL_YAGNI` | WIRED | `application.yml` route id `alias-patch` |
| `DownstreamCredentialMapper` | yagni-copilot | fallback map `/api/v1/patch` | WIRED | `DownstreamCredentialMapperTest` |
| `PortfolioEmbeddingClient.embed` | `HashingTextEmbedder.embed` | `live == false` | WIRED | `PortfolioEmbeddingClient.java` lines 40-42 |

**Wiring:** 4/4 connections verified

## Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| AUTH-01 | 01-01 | Missing key and non-configured ey* / valid-test-token are rejected | satisfied | `libs/shared/src/test/java/com/portfolio/shared/security/ApiKeyAuthFilterTest.java` |
| PATCH-01 | 01-01 | Keyed patch with no OpenAI key is offline HTTP 200 | satisfied | `YagniCopilotApplicationTest.patchOk` |
| PATCH-02 | 01-01 | Concurrent `propose` does not throw and matches baseline metrics | satisfied | `YagniPatchServiceTest.concurrentProposesDoNotThrow` |
| GATE-01 | 01-01 | `/api/v1/patch` resolves to yagni-copilot | satisfied | `DownstreamCredentialMapperTest.fallsBackWhenYamlEmpty` |
| EMBED-01 | 01-01 | Hashing embedder when key or model is absent | satisfied | `PortfolioEmbeddingClientTest.hashesWhenKeyOrModelMissing` |

**Coverage:** 5/5 requirements satisfied

## Suite

`./mvnw -B test` on 2026-10-03: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. The count includes `YagniPatchServiceTest.concurrentProposesDoNotThrow`. The earlier 117-test run was this suite before that test existed.

## Patch burst

Docker rerun of only `POST /api/v1/patch`, 50 concurrent, through the gateway at `2026-10-02T22:51:57Z`: 50/50 HTTP 200, 0 errors, p50 34 ms, p95 50 ms, accuracy HTTP 200 with `mode` offline, auth 401/401/401. Recorded in `docs/stress-results.md`. The other routes from the earlier full sample were not rerun.

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | none on the patch path | — | `measure` does not catch parser failures. The controller does not swallow them. |

**Anti-patterns:** 0 blockers

## Human Verification Required

None.
