# Project Catalog Proposal — Intermediate → Expert Spring AI Portfolio

**Status:** Catalog adopted. Folder migrate applied per `FOLDER-STRUCTURE-PROPOSAL.md`. Do not treat this file as permission to scaffold apps; scaffold only when asked.  
**Audience:** Human + coding agents deciding *what to build next*.  
**Sources (L0):** [spring-ai](https://github.com/spring-projects/spring-ai) · [spring-ai-examples](https://github.com/spring-projects/spring-ai-examples) · [Spring AI reference 2.0.x](https://docs.spring.io/spring-ai/reference/)  
**Date:** 2026-09-22  
**Harness model:** Multi plugin/skill/connector per project (not 1:1).

---

## Gate facts (create)

1. **Callers:** No runtime code calls this file. It is a human/agent planning artifact. Nothing in-tree currently references `PROJECT-CATALOG-PROPOSAL.md` (grep empty). Optional later links from `ARCHITECTURE-LAYERS.md` / `TODO.md` are documentation-only.
2. **Uniqueness:** No `PROJECT-CATALOG*.md` exists. `ARCHITECTURE-LAYERS.md` / `TODO.md` describe current messy modules + layers; they are not a multi-skill product catalog.
3. **Data I/O:** None — Markdown proposal only (no schema, dates, or data files).
4. **User instruction (verbatim excerpt):** *"May write ONE new planning doc if useful: e.g. `Agentic-AI-Expert-Portfolio/PROJECT-CATALOG-PROPOSAL.md` (proposal only)."* Plus correction: *"NOT 1 plugin per project. For each Spring AI project you may assign and use multiple Cursor plugins, skills, AND connectors together"* and *"Rewrite/overwrite PROJECT-CATALOG-PROPOSAL.md if you already started with 1:1 mapping."*

---

## 1. Clear separation (read this first)

- **Platform (L0)** = Spring AI + Spring Boot. ChatClient, Advisors, Tools/`ToolCallingAdvisor`, MCP client/server, RAG (`QuestionAnswerAdvisor` / `RetrievalAugmentationAdvisor`), Chat Memory, Evaluation (`RelevancyEvaluator`, `FactCheckingEvaluator`), streaming, multimodal. Patterns live in **spring-ai-examples**, not in Cursor plugins.
- **Plugin / skill / connector (L1)** = Cursor **build harness** used *while writing* the app (YAGNI, UX rules, SDD, review, MCP brokers, scaffolding). **Multiple packs per project are normal and preferred.**
- **Project (L2)** = the shippable Spring Boot product. Health endpoints report readiness — **never** plugin names, skill brands, or `framework: spec-kit`.

**Version target for new work (decisive):**

| Item | Target |
|---|---|
| Java | **21** (LTS; Virtual Threads OK) |
| Spring Boot | **4.0.x / 4.1.x** with Spring AI **2.0.x** (official: AI 2.0.x ↔ Boot 4.x) |
| Alternate path | Boot **3.4+/3.5** + Spring AI **1.1.x** if Boot 4 is deferred — **do not** stay on `1.0.0-M2` for new expert work |
| Examples baseline | spring-ai-examples ~Java 17 / Boot 3.4.5 / AI ~1.1 — port patterns forward; use `spring-ai-starter-model-*` |

Current messy modules are pinned Boot 3.3.3 + AI `1.0.0-M2`. This catalog assumes a **coordinated upgrade** for new product shapes; existing folders are out of scope until you provide structure.

---

## 2. L1 packs (compose freely)

| Pack | Upstream | Roles when composing a project |
|---|---|---|
| **ponytail** | [dietrichgebert/ponytail](https://github.com/dietrichgebert/ponytail) | YAGNI / minimal diffs / AST bounds |
| **ui-ux-pro-max** | [nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) | Design CSVs, typography/color/stack rules |
| **agent-skills** | [addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) | Review, debugging, relevancy evals |
| **spec-kit** | [github/spec-kit](https://github.com/github/spec-kit) | Spec-driven: specify → plan → tasks |
| **one-skill-to-rule-them-all** | [rebelytics/one-skill-to-rule-them-all](https://github.com/rebelytics/one-skill-to-rule-them-all) | Task observation, skill discovery |
| **antigravity-awesome-skills** | [benjaminasterA/antigravity-awesome-skills](https://github.com/benjaminasterA/antigravity-awesome-skills) | Scaffolding / blueprint catalog |
| **gstack** | [garrytan/gstack](https://github.com/garrytan/gstack) | Agent routing, browser QA, design review |
| **paper2code** | [going-doer/paper2code](https://github.com/going-doer/paper2code) | Paper→algorithm workflow/knowledge (not Java runtime) |
| **system-design-primer** | [donnemartin/system-design-primer](https://github.com/donnemartin/system-design-primer) | C4, capacity, ADRs, pattern library |
| **sentinel-ai-offensive** *(stretch)* | workspace `.agents/plugins/sentinel-ai-offensive` | Authorized security review habits — never a runtime badge |

**Connectors (L1/infra):** OpenAI / Anthropic / Ollama; PGVector / Redis / Chroma; GitHub MCP; Brave / filesystem MCP; Prometheus / Grafana / OTel; Docker Compose; Spring Cloud Gateway; IdP (OAuth2/OIDC).

---

## 3. Official Spring AI surface mapped here

| Area | Official anchors |
|---|---|
| **Agentic workflows** | `agentic-patterns/`: chain, parallelization, routing, orchestrator-workers, evaluator-optimizer |
| **Advisors** | `advisors/`: recursive, evaluation-recursive, tool-argument-augmenter; RAG advisors in reference |
| **MCP** | `model-context-protocol/`: client-starter, filesystem, weather, web-search, annotations, sampling, dynamic-tool-update, agents gateway |
| **Agents** | `agents/reflection` |
| **Tools** | `misc/spring-ai-java-function-callback`; 2.0 `ToolCallingAdvisor` |
| **RAG** | `QuestionAnswerAdvisor`, `RetrievalAugmentationAdvisor` + modular RAG; `kotlin/rag-with-kotlin` |
| **Memory** | `MessageChatMemoryAdvisor`; JDBC/Redis/Cassandra/Neo4j/Mongo repositories |
| **Evaluation** | `RelevancyEvaluator`, `FactCheckingEvaluator` |
| **Streaming / multimodal** | Chat streaming + image/audio model starters |
| **Observability** | Spring AI observability + Micrometer |

---

## 4. Recommended project catalog (10 products)

---

### P1 — YAGNI Coding Copilot API
**Level:** Intermediate → Advanced

**What you build:** REST/SSE service: Java change request + snippet → minimal patch with cyclomatic/AST bounds via tools — not a 200-line “refactor” chat wrapper.

**Primary Spring AI capabilities:** ChatClient; Tools / `@Tool` + `ToolCallingAdvisor`; Advisors; Structured Output; Streaming; optional `MessageChatMemoryAdvisor`.

**Plugin/skill pack(s):**
- **ponytail** — YAGNI / build discipline
- **agent-skills** — review/debug after generation
- **spec-kit** — ticket → spec/tasks before coding
- **gstack** *(optional)* — API/browser smoke when agent claims done

**Connectors:** OpenAI or Anthropic; optional Ollama; JavaParser as deterministic tool; GitHub MCP; Micrometer → Prometheus.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · WebMVC · OpenAPI · Docker · optional Redis session memory.

**Why portfolio-worthy:** Deterministic fences + Spring AI tools — engineering, not prompt theater.

**Study:** `misc/spring-ai-java-function-callback`, `advisors/tool-argument-augmenter-demo`, `agents/reflection`.

---

### P2 — Design-System RAG Studio
**Level:** Intermediate → Advanced

**What you build:** Ingest design CSVs into a vector store; generate stack-specific UI specs (tokens, layout, a11y) with streaming JSON; retrieval-grounded refusal of generic defaults.

**Primary Spring AI capabilities:** Document ETL; VectorStore; `RetrievalAugmentationAdvisor` / `QuestionAnswerAdvisor`; RewriteQueryTransformer; Structured Output; Streaming; `RelevancyEvaluator` in tests.

**Plugin/skill pack(s):**
- **ui-ux-pro-max** — knowledge pack + design skill
- **ponytail** — keep generators thin
- **gstack** — design-html / visual review
- **agent-skills** — relevancy/eval habits

**Connectors:** PGVector; OpenAI embeddings + chat; optional Redis semantic cache; object storage for assets.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · `spring-ai-rag` · PGVector · Flyway · Docker Compose · optional Kotlin templates.

**Why portfolio-worthy:** Modular RAG with measurable relevancy — rare in Java portfolios.

**Study:** RAG reference; `kotlin/rag-with-kotlin`; Evaluation Testing docs.

---

### P3 — Engineering Quality Gate (Hybrid Deterministic + LLM)
**Level:** Advanced

**What you build:** CI-facing gate: static/SAST first; LLM for explanation, prioritization, patches; evaluator-optimizer until structured PASS/FAIL.

**Primary Spring AI capabilities:** Evaluator-optimizer; Structured Output; Tools for analyzers; Evaluation API; Advisors (injection hardening).

**Plugin/skill pack(s):**
- **agent-skills** — review/SAST/eval SoT
- **ponytail** — prevent whole-repo “fixes”
- **spec-kit** — fail criteria as acceptance specs
- **sentinel-ai-offensive** *(stretch, build-time only)* — audit checklist inspiration; never ship offensive runtime

**Connectors:** GitHub Actions / GitHub MCP; SpotBugs/PMD CLI tools; Ollama for cheap `FactCheckingEvaluator`; Prometheus on gate outcomes.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · async jobs · JUnit + Testcontainers · Docker.

**Why portfolio-worthy:** Hybrid intelligence (deterministic > decorative LLM).

**Study:** `agentic-patterns/evaluator-optimizer`; `advisors/evaluation-recursive-advisor-demo`.

---

### P4 — Spec-Driven Multi-Agent Delivery Orchestrator
**Level:** Advanced → Expert

**What you build:** Brief → Spec Kit-shaped artifacts → orchestrator-workers + routing to specialists (API, data, UI, tests) → persisted runs + streamed progress.

**Primary Spring AI capabilities:** Orchestrator-workers; Routing; Chain; Structured Output; JDBC Chat Memory; Streaming; Tools for artifact I/O; optional filesystem MCP.

**Plugin/skill pack(s):**
- **spec-kit** — SDD workflow + artifact shapes
- **antigravity-awesome-skills** — worker blueprints
- **one-skill-to-rule-them-all** — observe winning worker paths
- **ponytail** — constrain task scope
- **ui-ux-pro-max** — when UI worker is routed
- **gstack** — E2E audit of delivered UI/API

**Connectors:** Postgres (runs + memory); GitHub MCP; filesystem MCP; OpenAI/Anthropic; OTel per agent span; Redis locks/queue.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · SSE · Postgres · Redis · Docker.

**Why portfolio-worthy:** Anthropic-style agentic patterns idiomatically in Spring AI.

**Study:** `agentic-patterns/orchestrator-workers`, `routing-workflow`, `chain-workflow`, `parallelization-workflow`.

---

### P5 — Agent Observability & Skill Evolution Console
**Level:** Advanced

**What you build:** Records trajectories (prompts, tools, outcomes), clusters failures, proposes skill/prompt deltas — real meta-learning, not a dead ChatClient.

**Primary Spring AI capabilities:** Custom observation advisors; Chat Memory repositories; Tools; Structured Output; Evaluation loops; Observability; optional SemanticCacheAdvisor.

**Plugin/skill pack(s):**
- **one-skill-to-rule-them-all** — observation / skill-discovery methodology
- **agent-skills** — debug + eval rubrics
- **gstack** — plan-tune / benchmark review of proposed changes
- **spec-kit** — observations → change specs before apply

**Connectors:** OpenTelemetry + Prometheus/Grafana; Postgres or ClickHouse; Redis; model provider for summaries.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · Micrometer Tracing · Grafana · Docker.

**Why portfolio-worthy:** Trajectory analytics is what production AI teams need.

**Study:** Spring AI observability; `agents/reflection`; memory repository docs.

---

### P6 — MCP Tool Marketplace & Secure Broker
**Level:** Expert

**What you build:** Spring MCP server + client hub: register tools with schemas/scopes, dynamic tool updates; ChatClient via `ToolCallbackProvider`; admin catalog UI — not “gstack is the app.”

**Primary Spring AI capabilities:** MCP client/server (annotations, sampling); `ToolCallbackProvider`; dynamic-tool-update; ChatClient + memory; Streaming.

**Plugin/skill pack(s):**
- **gstack** — tool/router taxonomy + browser QA of admin
- **agent-skills** — privilege-boundary review
- **ponytail** — minimal broker surface
- **spec-kit** — tool registration contracts
- **antigravity-awesome-skills** — seed catalog categories

**Connectors:** MCP STDIO/SSE; Brave search MCP; filesystem MCP; OAuth2/API keys; Redis rate limits; Docker agents-gateway pattern.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x MCP starters · Spring Security · Postgres · Redis · Docker Compose · optional WebFlux.

**Why portfolio-worthy:** MCP is Spring AI 2.0’s headline; a secure broker beats weather hello-world.

**Study:** `model-context-protocol/*` (client-starter, dynamic-tool-update, web-search, mcp-annotations, brave-docker-agents-gateway).

---

### P7 — Blueprint → Running Spring AI App Factory
**Level:** Intermediate → Advanced

**What you build:** Pick blueprint (CRUD API, RAG chatbot, MCP client) → generate Boot project zip with correct AI starters, Compose, smoke tests.

**Primary Spring AI capabilities:** ChatClient; Structured Output; Tools (sandbox writers); Chain (plan → generate → verify); Evaluation (“README matches POM?”).

**Plugin/skill pack(s):**
- **antigravity-awesome-skills** — blueprint scaffolding SoT
- **spec-kit** — blueprint → implementable tasks
- **ponytail** — strip over-generation
- **ui-ux-pro-max** — when blueprint includes frontend
- **gstack** — post-gen smoke / design review
- **agent-skills** — review generated CI/security defaults

**Connectors:** GitHub (template repos); Docker; OpenAI; filesystem sandbox.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · templates · Zip · Testcontainers · Docker.

**Why portfolio-worthy:** Proves platform literacy (BOM, Boot 4 starters) teams lack after M2 tutorials.

**Study:** Getting Started (BOM 2.0); client-starter module structure.

---

### P8 — Paper → Java Algorithm Lab (Advanced RAG + Synthesis)
**Level:** Expert

**What you build:** Ingest papers; Modular RAG (rewrite + multi-query); evaluator-optimizer synthesizes Java prototype + tests with citations. paper2code = harness/knowledge only.

**Primary Spring AI capabilities:** Document ETL; advanced `RetrievalAugmentationAdvisor`; MultiQueryExpander; FactChecking + Relevancy evaluators; Evaluator-optimizer; Tools (compile/test sandbox); Structured Output.

**Plugin/skill pack(s):**
- **paper2code** — synthesis methodology + sample data
- **agent-skills** — quality on generated algorithms
- **ponytail** — refuse sprawling frameworks from a short paper
- **spec-kit** — claims → verifiable tasks/tests
- **system-design-primer** — when papers imply distributed systems

**Connectors:** PGVector; Tika/PDF; Ollama fact-check; Docker `mvn test` sandbox; optional Python sidecar for niche numeric libs (explicit boundary).

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · PGVector · Tika · Testcontainers · Docker · optional Kotlin scripts.

**Why portfolio-worthy:** Expert RAG + eval gates; avoids wrapping Python paper2code in a JAR.

**Study:** Modular RAG reference; `agentic-patterns/evaluator-optimizer`; `kotlin/rag-with-kotlin`.

---

### P9 — System Architect Co-Pilot (C4 / ADR / Capacity)
**Level:** Advanced → Expert

**What you build:** Requirements → C4 (Structurizr/Mermaid), ADRs, capacity estimates grounded in vectorized design corpus; parallel workers for security/scalability/cost.

**Primary Spring AI capabilities:** Parallelization + orchestrator-workers; RAG; Structured Output; Tools (diagram/cost); Chat Memory; Streaming.

**Plugin/skill pack(s):**
- **system-design-primer** — architecture knowledge pack
- **spec-kit** — requirements → ADR workflow
- **gstack** — design review of architecture docs
- **agent-skills** — challenge weak tradeoffs
- **ui-ux-pro-max** — architecture viewer UI
- **one-skill-to-rule-them-all** — capture recurring decision patterns

**Connectors:** PGVector; Structurizr/Mermaid; GitHub (commit ADRs); OTel; optional Neo4j service graphs.

**Tech stack:** Java 21 · Boot 4.x · Spring AI 2.0.x · PGVector · Web · Docker · optional Neo4j.

**Why portfolio-worthy:** Staff-level ADRs + capacity, not “just use Kubernetes.”

**Study:** `agentic-patterns/parallelization-workflow`, `orchestrator-workers`; RAG advisors.

---

### P10 — Enterprise AI Edge Platform (Gateway + Policy)
**Level:** Advanced (platform — no skill product identity)

**What you build:** Spring Cloud Gateway: auth, rate limits, mTLS, routing to P1–P9, resilience, unified observability. **No plugin badge.**

**Primary Spring AI capabilities:** Indirect — no ChatClient on the edge; SSE fan-in + request IDs for conversation correlation downstream.

**Plugin/skill pack(s):**
- **agent-skills** — security/review while hardening
- **ponytail** — avoid config sprawl
- **spec-kit** — threat model + route policy specs
- **gstack** — E2E audits across routed apps
- **system-design-primer** — capacity / failure modes for the edge

**Connectors:** Spring Cloud Gateway; Redis rate limiter; Prometheus/Grafana; certs; Docker/K8s; OAuth2/OIDC IdP.

**Tech stack:** Java 21 · Boot matching Cloud train · Spring Cloud Gateway · Redis · Micrometer · Helm/Compose.

**Why portfolio-worthy:** Ships *systems*, not only chat demos; keeps L1 out of `/health`.

**Study:** examples’ Docker agents gateway (pattern inspiration); Spring Cloud Gateway docs.

---

## 5. Stretch (optional after P1–P10)

| Idea | Level | Harness mix | Spring AI angle |
|---|---|---|---|
| **S1 Multimodal Support Desk** | Advanced | ui-ux-pro-max, gstack, agent-skills, ponytail | Image + chat, streaming, memory |
| **S2 Kotlin RAG Microservice** | Intermediate | ponytail, agent-skills, spec-kit | Productionize `kotlin/rag-with-kotlin` |
| **S3 AI-Validated Integration Harness** | Expert | agent-skills, spec-kit, one-skill-to-rule-them-all | Mirror examples’ AI validation for your apps |
| **S4 Authorized Security Review Assistant** | Expert | sentinel, agent-skills, ponytail, spec-kit | Tools + advisors; deterministic scope; no offensive runtime |

---

## 6. Explicitly deprecate / do not carry forward

1. Ten siloed “Completed” modules whose maturity is offline tests.
2. Plugin / skill / “framework” strings on `/health`.
3. Treating 1:1 plugin ↔ module as product identity.
4. Dead ChatClient beans labeled “AI-wired.”
5. Paper2code as Python runtime inside a Java module.
6. Gateway labeled with skill badges.
7. Staying on Spring AI `1.0.0-M2` + Boot 3.3.3 for *new* expert work.
8. Force-fitting sentinel (or random cache skills) into every product.
9. Marketing “Enterprise Agentic Suite” without Evaluator/RAG/MCP evidence.
10. Thin demos that only wrap `ChatClient.prompt()` with no advisors, tools, memory, or eval.

---

## 7. Suggested build order (no folder layout)

```text
0. Pin Boot 4 + Spring AI 2.0.x BOM (or explicit Boot 3.5 + AI 1.1.x)
1. P10 Edge — auth, routing, observability contracts (backends can be stubs)
2. P1 YAGNI Copilot — Tools + Advisors honesty
3. P2 Design RAG Studio — VectorStore + Evaluation tests
4. P3 Quality Gate — evaluator-optimizer + CI connector
5. P6 MCP Broker — unlocks tool ecosystem
6. P4 Spec Orchestrator — needs P6 + P1/P3 worker patterns
7. P7 App Factory — codifies starters from 1–6
8. P8 Paper Lab — hardest RAG + sandbox
9. P5 Observability Console — needs real trajectories
10. P9 Architect Co-Pilot — corpus RAG + parallel workers
Then S1–S4 as depth, not prerequisites.
```

**Shared foundation (conceptual):** parent BOM, shared errors/security, Compose for Postgres/PGVector/Redis, Prometheus that actually binds meters, offline profile that fails closed.

---

## 8. Mapping note vs current modules 01–10

This catalog **redefines product outcomes**. Legacy `01≈P1` … `10≈P10` source sits in `archive/legacy-portfolio/`. Live placeholders are `apps/<kebab>/`. Do not implement inside the archive.

---

## 9. Ready for your folder structure

Catalog and multi-skill harness composition are decided. Next input from you:

- Monorepo vs multi-repo layout  
- Which of P1–P10 are in-scope for v1  
- Final pin: Boot 4 + AI 2.0.x vs Boot 3.5 + AI 1.1.x  

Folder structure is in place. Remaining choices for v1: which of P1–P10 to scaffold first, and the final Boot/AI pin.
