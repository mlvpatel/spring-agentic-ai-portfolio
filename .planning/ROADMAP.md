# Roadmap: Spring agentic AI portfolio

## Overview

v1 is the reactor that is already in the root `pom.xml`: `libs/shared` and fourteen apps, with the edge gateway in front. This milestone records that shipped tree. It does not add a second product.

## Milestones

## v1.0: Shipped portfolio

Definition of done: AUTH-01, PATCH-01, PATCH-02, GATE-01, and EMBED-01 are checked in `REQUIREMENTS.md`, named in `phases/01-shipped-portfolio/01-VERIFICATION.md`, and listed in `01-01-SUMMARY.md` `requirements-completed`.

### Phase 1: Shipped portfolio

**Goal**: The live reactor answers keyed offline requests, the gateway routes `/api/v1/patch` to yagni-copilot, and concurrent patch parses do not share one `JavaParser`.
**Depends on**: Nothing (first phase)
**Requirements**: AUTH-01, PATCH-01, PATCH-02, GATE-01, EMBED-01
**Success Criteria** (what must be TRUE):
  1. A patch request with no API key is rejected, including `ey*` and `valid-test-token` bearers that are not the configured key.
  2. A keyed `POST /api/v1/patch` with no OpenAI key returns an offline patch proposal.
  3. Parallel `propose` calls on one `YagniPatchService` do not throw and agree on cyclomatic complexity and AST depth.
  4. `/api/v1/patch` maps to yagni-copilot.
  5. Embedding falls back to the hashing embedder when no key or embedding model is present.
**Plans**: 1 plan

Plans:
- [x] 01-01: Record the shipped reactor and the per-request JavaParser fix

## Progress

**Execution Order:**
Phase 1 only.

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Shipped portfolio | 1/1 | Complete | 2026-10-03 |
