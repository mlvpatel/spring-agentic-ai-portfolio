# Spring agentic AI portfolio

## What This Is

A local reactor of Spring Boot apps behind one edge gateway. Each app shows one agentic pattern (patch proposal, retrieval, quality gate, spec run, and the rest of the apps in the root `pom.xml`) and stays usable with no paid model key.

## Core Value

A keyed request to a live app returns a real offline answer, and the gateway can send that request to the right app.

## Requirements

### Validated

- API key gate on the shared filter and on each live app's tests
- Offline `POST /api/v1/patch` proposal
- Patch parsing that stays consistent when many calls share one service
- Gateway alias from `/api/v1/patch` to yagni-copilot
- Hashing embeddings when no OpenAI key or embedding model is present

### Active

None. v1 is the shipped reactor described in `ROADMAP.md`.

### Out of Scope

- Cloud cluster deploy and image registry publish. Nothing in this tree ships images to a registry or applies manifests to a cluster.
- A certified latency SLO. `docs/stress-results.md` is a laptop curl sample.

## Context

Imported on 2026-10-03 from `TODO.md` and the apps already listed in the root `pom.xml` (`libs/shared` plus fourteen apps). Parent BOM: Spring Boot 4.1.1, Spring AI 2.0.1, Spring Cloud 2025.1.3, Java 21. Default tests leave the chat and embedding models at `none`.

The 2026-09-28 milestone audit was `gaps_found` because this baseline did not exist yet.

## Constraints

- **Runtime**: Java 21. Tests run with `./mvnw -B test` and must not call OpenAI.
- **Auth**: `app.security.api-key` is required at startup. Requests need the configured key. `ey*` Bearer and `valid-test-token` are not accepted unless that string is the configured key.
- **Publish**: Public git history is `mlvpatel/spring-agentic-ai-portfolio`. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, or `.env`.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| One `ApiKeySecurityAutoConfiguration` for a blank key | Thirteen copied startup validators were the same check | Shipped |
| Gateway aliases are a static map | Path collisions were the earlier stress gap | Shipped |
| `HashingTextEmbedder` when no embedding model | Unit tests must not call the network | Shipped |
| New `JavaParser` inside `YagniPatchService.measure` | javaparser-core 3.26.1 keeps one `GeneratedJavaParser` per `JavaParser` | Shipped 2026-10-03 |

---
*Last updated: 2026-10-03 after import from TODO.md and the live reactor*
