---
phase: 01
slug: shipped-portfolio
status: validated
nyquist_compliant: true
wave_0_complete: true
created: 2026-10-03
---

# Phase 01 — Validation strategy

Per-phase validation contract. Reconstructed from the shipped tests after phase 1 was already verified. No new requirements.

## Test infrastructure

| Property | Value |
|----------|-------|
| Framework | JUnit 5 (spring-boot-starter-test), Maven Surefire |
| Config file | root `pom.xml` |
| Quick run command | `./mvnw -B test` |
| Full suite command | `./mvnw -B test` |
| Estimated runtime | 35.59 s wall time on 2026-10-06 |

## Sampling rate

- After every task commit: `./mvnw -B test`
- After every plan wave: `./mvnw -B test`
- Before `/gsd-verify-work`: full suite must be green
- Max feedback latency: 35.59 s wall time for `./mvnw -B test` on 2026-10-06 (`/usr/bin/time -p` real). Maven reported 34.751 s. The 2026-10-03 run's wall time was not recorded.

## Per-task verification map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 01-01 | 01 | 1 | AUTH-01 | — | Missing key, `ey*`, and `valid-test-token` are rejected when they are not the configured key | unit | `./mvnw -B -pl libs/shared,apps/yagni-copilot,apps/ai-edge-gateway test` | ✅ | ✅ green |
| 01-01 | 01 | 1 | PATCH-01 | — | Keyed `POST /api/v1/patch` with no OpenAI key returns HTTP 200, `mode` offline | integration | `./mvnw -B -pl apps/yagni-copilot -Dtest=YagniCopilotApplicationTest#patchOk test` | ✅ | ✅ green |
| 01-01 | 01 | 1 | PATCH-02 | — | Concurrent `propose` on one service does not throw and matches the single-thread cyclomatic complexity and AST depth | unit | `./mvnw -B -pl apps/yagni-copilot -Dtest=YagniPatchServiceTest#concurrentProposesDoNotThrow test` | ✅ | ✅ green |
| 01-01 | 01 | 1 | GATE-01 | — | `/api/v1/patch` resolves to yagni-copilot | unit | `./mvnw -B -pl apps/ai-edge-gateway -Dtest=DownstreamCredentialMapperTest#fallsBackWhenYamlEmpty test` | ✅ | ✅ green |
| 01-01 | 01 | 1 | EMBED-01 | — | `embed` returns the hashing vector and does not call the model when the key or model is absent | unit | `./mvnw -B -pl libs/shared -Dtest=PortfolioEmbeddingClientTest#hashesWhenKeyOrModelMissing test` | ✅ | ✅ green |

Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky

The command that was actually run on 2026-10-03 is `./mvnw -B test`: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. That count includes `YagniPatchServiceTest.concurrentProposesDoNotThrow`. The per-row commands name the test that covers each requirement. They were not run again as separate invocations for this file.

The same full command on 2026-10-06, finished at 2026-10-06T00:32:52+02:00: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. `/usr/bin/time -p` real 35.59 s. Maven total time 34.751 s. Every file in public `668291d` matched the tree that ran. The per-row commands were not invoked separately.

Gateway patch burst, recorded in `docs/stress-results.md` at `2026-10-02T22:51:57Z`: 50 concurrent `POST /api/v1/patch` calls through the gateway, 50/50 HTTP 200, 0 errors, p50 34 ms, p95 50 ms. That burst is extra evidence for PATCH-02. It is not a sixth requirement.

## Wave 0 requirements

Existing infrastructure covers all phase requirements. The five tests above were already in the reactor when this file was written.

## Manual-only verifications

All phase behaviors have automated verification.

## Validation sign-off

- [x] All tasks have an automated verify command or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency under a recorded bound (35.59 s wall time on 2026-10-06, 118 tests, 0 failures)
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-10-03. Latency box signed 2026-10-06 after the green suite above.
