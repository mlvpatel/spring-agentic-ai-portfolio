---
phase: 01-shipped-portfolio
plan: 01
subsystem: api
tags: [spring-boot, gateway, javaparser]

requires: []
provides:
  - Keyed offline patch proposal at POST /api/v1/patch
  - Gateway alias from /api/v1/patch to yagni-copilot
  - Per-request JavaParser in YagniPatchService.measure
  - Hashing embedder when no embedding model is configured
affects: []

requirements-completed:
  - AUTH-01
  - PATCH-01
  - PATCH-02
  - GATE-01
  - EMBED-01

key-files:
  created: []
  modified:
    - apps/yagni-copilot/src/main/java/com/portfolio/yagni/service/YagniPatchService.java
    - apps/yagni-copilot/src/test/java/com/portfolio/yagni/service/YagniPatchServiceTest.java
---

# Phase 1 plan 01 summary

The reactor in the root `pom.xml` is the milestone. This plan records it and the 2026-10-03 parser fix.

`YagniPatchService` was a singleton with one `JavaParser`. javaparser-core 3.26.1 stores a single `GeneratedJavaParser` on that object and `getParserForProvider` calls `reset` on it. Concurrent `parse` calls tore the token state. A 1000-call test on 50 threads returned cyclomatic 1 instead of the single-thread baseline of 2. `measure` now builds a new `JavaParser` (`JAVA_21`) per call (`YagniPatchService.java` lines 84-87).

`./mvnw -B test`: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped.

Gateway patch burst after the fix: 50/50 HTTP 200, p50 34 ms, p95 50 ms (`docs/stress-results.md`, `2026-10-02T22:51:57Z`).
