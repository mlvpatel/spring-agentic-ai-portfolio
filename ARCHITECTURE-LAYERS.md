# Architecture layers — Agentic AI Expert Portfolio

**Purpose:** Keep platform, build harness, and portfolio products from being mixed up.  
**Audience:** Humans and coding agents working this workspace.  
**Related:** [`TODO.md`](./TODO.md) · [`FOLDER-STRUCTURE-PROPOSAL.md`](./FOLDER-STRUCTURE-PROPOSAL.md) · [`PROJECT-CATALOG-PROPOSAL.md`](./PROJECT-CATALOG-PROPOSAL.md) · [`AGENTS.md`](./AGENTS.md)

---

## How to read this workspace (one screen)

| Layer | What it is | Source of truth | What it is *not* |
|---|---|---|---|
| **L0 Platform** | Runtime stack every Java app builds on | Spring AI project + examples + official docs | Skills, Cursor plugins, GitHub “awesome” lists |
| **L1 Dev harness** | How agents build and review the code | `~/.cursor/skills/`, `~/.cursor/plugins/local/`, optional `../.agents/plugins/<name>/` | Runtime dependencies, health badges, product identity |
| **L2 Portfolio products** | Deployable apps P1–P10 | `apps/<kebab-name>/` | Proof that a skill “ships” because a string appears in JSON |

**Read order for implementers:** L0 docs/examples → L2 app code under `apps/` → L1 skill/plugin only when extending that app’s domain behavior.

Legacy numbered modules (`01–10`) live under `archive/legacy-portfolio/` for reference. They are not the live product layout.

---

## L0 — Platform (Spring AI)

Greenfield apps in `apps/` (except the edge gateway) are Spring Boot services that use Spring AI APIs (`ChatClient`, advisors, vector stores, tool calling, and so on as applicable). **P10** (`apps/ai-edge-gateway`) is Spring Cloud Gateway and does not embed Spring AI.

### Canonical sources (use these first)

| Role | URL |
|---|---|
| Framework source | https://github.com/spring-projects/spring-ai |
| Example patterns | https://github.com/spring-projects/spring-ai-examples |
| Official reference | https://docs.spring.io/spring-ai/reference/ |

When wiring RAG, advisors, tool calling, structured output, or observability: copy patterns from spring-ai-examples and the reference, not from Python skill repos or Cursor plugin READMEs.

### Version posture

| Item | Value |
|---|---|
| **New work (catalog)** | Java **21** · Boot **4.x** · Spring AI **2.0.x** (or Boot **3.4+/3.5** + AI **1.1.x** if Boot 4 is deferred) |
| **Parent POM today** | Boot **4.1.1** + Spring AI **2.0.1** + Spring Cloud **2025.1.3** |
| **Docs track** | Spring AI **2.0.x** on Boot **4.0.x / 4.1.x** |

Parent reactor also manages: springdoc **3.1.1**, JavaParser **3.26.1**, commons-csv **1.11.0**, Testcontainers BOM **1.21.3**, and `com.portfolio:shared-lib` (`libs/shared`).

---

## L1 — Dev harness / plugins

Primary Cursor locations:

```text
~/.cursor/skills/behuman/
~/.cursor/skills/find-skills/
~/.cursor/plugins/local/           # ponytail, spec-kit, gstack, …
```

Optional workspace sibling inventory (older path still useful for data packs):

```text
../.agents/plugins/<name>/
```

They are Cursor / agent capability packs used to implement, review, and ingest domain knowledge into L2 apps. They are not Maven dependencies, not runtime frameworks on `/health`, and not proof of “production ready.”

### Roles

| Role | Meaning |
|---|---|
| **Build agent** | Skills/commands that drive how the coding agent writes/refactors the app |
| **Knowledge pack** | Data/docs/CSV/solutions the app should ingest at build or runtime |
| **Workflow** | Spec/plan/eval loops the agent follows when changing the app |
| **Don't force-fit** | Present in inventory but not assigned to a product |

### Catalog map (multi-pack per product)

| Catalog app | Folder | Typical L1 packs (compose freely) |
|---|---|---|
| P1 YAGNI Copilot | `apps/yagni-copilot` | ponytail, agent-skills, spec-kit, optional gstack |
| P2 Design RAG Studio | `apps/design-rag-studio` | ui-ux-pro-max, ponytail, gstack, agent-skills |
| P3 Quality Gate | `apps/quality-gate` | agent-skills, ponytail, spec-kit |
| P4 Spec Orchestrator | `apps/spec-orchestrator` | spec-kit, antigravity-awesome-skills, one-skill-to-rule-them-all, ponytail |
| P5 Observability Console | `apps/agent-observability` | one-skill-to-rule-them-all, agent-skills, gstack, spec-kit |
| P6 MCP Broker | `apps/mcp-broker` | gstack, agent-skills, ponytail, spec-kit |
| P7 App Factory | `apps/app-factory` | antigravity-awesome-skills, spec-kit, ponytail |
| P8 Paper Algorithm Lab | `apps/paper-algorithm-lab` | paper2code (knowledge), agent-skills, ponytail, spec-kit |
| P9 System Architect | `apps/system-architect` | system-design-primer, spec-kit, gstack, agent-skills |
| P10 Edge Gateway | `apps/ai-edge-gateway` | build-time review only — **no** skill product identity |

Full product briefs: [`PROJECT-CATALOG-PROPOSAL.md`](./PROJECT-CATALOG-PROPOSAL.md).

**Skill adoption vocabulary (L1 → L2):** **None** | **Prompt** | **Data** | **Agent-built** — see [`TODO.md`](./TODO.md).

---

## L2 — Portfolio products (`apps/`)

Spring Boot applications under `apps/`. Maturity labels: **Scaffold** | **Engine-ready** | **AI-wired** | **Prod-blocked**.

Live reactor: P1–P10 plus stretch S1–S4 under `apps/`. Do not confuse them with the archived `01–10` engines.

| Catalog | Directory | L0 | Notes |
|---|---|---|---|
| P1 | `apps/yagni-copilot` | Spring AI | Tools + advisors honesty |
| P2 | `apps/design-rag-studio` | Spring AI | Modular RAG + relevancy tests |
| P3 | `apps/quality-gate` | Spring AI | Hybrid deterministic + LLM |
| P4 | `apps/spec-orchestrator` | Spring AI | Orchestrator-workers |
| P5 | `apps/agent-observability` | Spring AI | Trajectory analytics |
| P6 | `apps/mcp-broker` | Spring AI MCP | Secure tool broker |
| P7 | `apps/app-factory` | Spring AI | Blueprint → zip |
| P8 | `apps/paper-algorithm-lab` | Spring AI RAG | Java RAG; paper2code is harness/data only |
| P9 | `apps/system-architect` | Spring AI | C4 / ADR / capacity |
| P10 | `apps/ai-edge-gateway` | Spring Cloud Gateway | Ingress only — no skill badge |
| S1 | `apps/multimodal-support-desk` | Spring AI (offline) | Ticket triage + image meta |
| S2 | `apps/kotlin-rag-microservice` | Kotlin + Spring | Offline corpus RAG |
| S3 | `apps/ai-validated-integration-harness` | Spring | Contract validation |
| S4 | `apps/security-review-assistant` | Spring | Authorized checklist review |

Shared helpers: `libs/shared` (`com.portfolio:shared-lib`).

---

## Layer diagram

```text
┌─────────────────────────────────────────────────────────────────┐
│ L1 Dev harness (~/.cursor/* + optional ../.agents/plugins)      │
│   builds / reviews / ingests knowledge into ↓                   │
├─────────────────────────────────────────────────────────────────┤
│ L2 Portfolio products (apps/P1–P9 agents + apps/P10 gateway)    │
│   run on ↓                                                      │
├─────────────────────────────────────────────────────────────────┤
│ L0 Platform: Spring Boot + Spring AI (+ Gateway for P10)        │
│   SoT: spring-ai · spring-ai-examples · docs.spring.io/spring-ai│
└─────────────────────────────────────────────────────────────────┘
```

L1 never appears as a Maven coordinate. L0 never gets renamed after a plugin. L2 never claims “Completed / Production Ready” because an L1 folder exists or offline tests pass in `archive/`.

---

## Explicit anti-patterns (do not restore)

| Anti-pattern | Why it’s wrong | Correct framing |
|---|---|---|
| Plugin string in health (`"plugin": "ponytail"`, …) | Treats L1 as L2 identity | Health reports readiness; document build skills in README/TODO |
| “Completed because tests pass offline” | Engines + mocks ≠ live AI or prod | Use Scaffold / Engine-ready / AI-wired / Prod-blocked |
| Treating archive `01–10` as the live tree | Migrate moved them out of the product path | Build in `apps/`; read archive only for history |
| Conflating paper2code Python with P8 | Upstream pack is knowledge/workflow; app is Java RAG | Ingest data; implement RAG with Spring AI |
| Gateway labeled with skill badges | P10 is edge platform | Auth, routing, mTLS, rate limits only |
| Skills as platform SoT | Spring AI examples/docs are L0 | Skills guide *how we build*; Spring AI defines *what we build on* |
| Force-fitting sentinel into every product | Inventory ≠ product map | Optional build hygiene only |
| Half-upgrading Spring AI mid-scaffold | BOM churn | Coordinated Boot+AI pin when scaffolding greenfield |
| Dead ChatClient = “AI-wired” | Injection without call sites is scaffolding | Call or remove; register declared tools where claimed |

---

## Recommended build order

1. Keep archive frozen; do not put `01–10` back on the reactor.
2. Pin Boot 4 + Spring AI 2.0 (or explicit Boot 3.5 + AI 1.1) for new apps.
3. Scaffold **P10** edge (auth/routing/observability contracts; backends can be stubs) or **P1** YAGNI Copilot first.
4. Continue catalog order in [`PROJECT-CATALOG-PROPOSAL.md`](./PROJECT-CATALOG-PROPOSAL.md) section 7.
5. Raise L1→L2 adoption Prompt → Data → Agent-built per app.

---

## Quick checks for agents

- Before adding a feature to an app: open spring-ai-examples / reference for the Spring AI API pattern.
- Before claiming a skill “drives” an app: confirm files under `~/.cursor/` or `../.agents/plugins/<name>/` are actually used (Prompt/Data/Agent-built).
- Never add a plugin name to a health JSON payload as proof of integration.
- Never treat P8 as a Python paper2code port; treat it as Java RAG on L0.
- Never implement inside `archive/legacy-portfolio/` for new product work.
