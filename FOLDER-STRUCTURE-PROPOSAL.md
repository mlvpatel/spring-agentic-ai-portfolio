# Folder structure — secure + vibe-coding Spring AI portfolio

**Status:** Adopted. Legacy `01–10` migrated into `archive/legacy-portfolio/` (2026-09-22).  
**Audience:** Humans and coding agents.  
**Related:** [`AGENTS.md`](./AGENTS.md) · [`PROJECT-CATALOG-PROPOSAL.md`](./PROJECT-CATALOG-PROPOSAL.md) · [`ARCHITECTURE-LAYERS.md`](./ARCHITECTURE-LAYERS.md)  
**Date:** 2026-09-22

---

## 1. Goals

### Secure

| Goal | Meaning |
|---|---|
| Secrets never in git | `.env` gitignored; commit `.env.example` only; no `.p12` / private keys / API tokens |
| Blast-radius isolation | Edge gateway separate from product apps; shared libs cannot pull secrets |
| Infra ≠ product | Compose/Helm/monitoring under `infra/`; apps declare needs only |
| Test ≠ prod | Fixtures in test resources; prod via env / secret mounts |
| Harness ≠ runtime | Skills/plugins stay in Cursor (`~/.cursor/…`); not under `apps/` |

### Vibe coding

| Goal | Meaning |
|---|---|
| Clear boundaries | `apps/` / `libs/` / `infra/` / `docs/` / `archive/` |
| Short paths | `apps/yagni-copilot/` not `01-spring-ai-lazy-dev-agent/` |
| Discoverable context | Thin root `AGENTS.md` + commands + rules |
| Skill discovery | find-skills at `~/.cursor/skills/find-skills/` + `/find-skills` |
| Human prose | behuman on every markdown edit |

---

## 2. Unified tree (now)

```text
Agentic-AI-Expert-Portfolio/
├── AGENTS.md
├── CLAUDE.md                      # thin pointer → AGENTS.md
├── README.md
├── .env.example
├── .gitignore
├── pom.xml                        # reactor: libs/shared only
│
├── .cursor/
│   ├── settings.json
│   ├── commands/
│   ├── rules/
│   ├── agents/
│   ├── skills/                    # keep empty; prefer ~/.cursor/skills
│   └── hooks/
│
├── apps/                          # P1–P10 placeholders + README each
│   ├── yagni-copilot/
│   ├── design-rag-studio/
│   ├── quality-gate/
│   ├── spec-orchestrator/
│   ├── agent-observability/
│   ├── mcp-broker/
│   ├── app-factory/
│   ├── paper-algorithm-lab/
│   ├── system-architect/
│   └── ai-edge-gateway/
│
├── libs/shared/                   # Maven module shared-lib
├── infra/{compose,helm,monitoring,scripts}/
├── docs/
├── archive/legacy-portfolio/      # 01–10 frozen source
├── scripts/
└── planning docs (ARCHITECTURE-LAYERS, PROJECT-CATALOG, TODO, …)
```

### Outside this repo (L1 SoT)

```text
~/.cursor/skills/behuman/
~/.cursor/skills/find-skills/
~/.cursor/plugins/local/           # ponytail, spec-kit, gstack, …
```

Do not dump skill packs into `apps/`.

---

## 3. Per-app skeleton (when implementing)

```text
apps/<name>/
├── AGENTS.md
├── README.md
├── CODEMAP.md
├── pom.xml
├── .env.example
└── src/main/{java,resources}  src/test/…
```

Gateway (P10): no Spring AI starters. Other apps: Boot + Spring AI per catalog pins.

---

## 4. Security policies

1. Secrets: examples only; CI/script may scan for `sk-` / private-key patterns.
2. Certs: generate under gitignored dirs via `infra/scripts/`.
3. `infra/` vs `apps/`: topology vs product logic.
4. No nested `.agents/` or plugin trees under products.
5. Only `ai-edge-gateway` terminates public traffic when wired.
6. `libs/shared`: no API keys, no default Bearer bypasses.
7. Archive is read-only and off the reactor path after migrate.
8. No shadow MCP install from agents; limit MCP scope when configured.
9. `AGENTS.md` stays short and secret-free.

---

## 5. Cursor harness map (Claude Code → Cursor)

| Claude Code | Cursor (this repo) |
|---|---|
| `CLAUDE.md` primary | `AGENTS.md` primary; `CLAUDE.md` pointer |
| `.claude/` | `.cursor/` |
| `.claude/commands/` | `.cursor/commands/` |
| `.claude/skills/` | Prefer `~/.cursor/skills/` (+ optional empty `.cursor/skills/`) |
| `.claude/agents/` | `.cursor/agents/*.yml` |
| `.mcp.json` | Optional later; do not add unapproved servers |

---

## 6. P1–P10 folder names

| Catalog | Folder |
|---|---|
| P1 | `apps/yagni-copilot` |
| P2 | `apps/design-rag-studio` |
| P3 | `apps/quality-gate` |
| P4 | `apps/spec-orchestrator` |
| P5 | `apps/agent-observability` |
| P6 | `apps/mcp-broker` |
| P7 | `apps/app-factory` |
| P8 | `apps/paper-algorithm-lab` |
| P9 | `apps/system-architect` |
| P10 | `apps/ai-edge-gateway` |

Legacy map: `01`→P1 … `10`→P10; `shared-lib`→`libs/shared`.

---

## 7. Migrate policy (still gated)

**Migrate done (human-approved 2026-09-22).** Legacy `01–10` are under `archive/legacy-portfolio/`. Reactor lists `libs/shared` only. `apps/` placeholders + infra moves are in place. Next: scaffold P10 or P1 application code (not part of this migrate).

---

## 8. Vibe conventions

| Convention | Rule |
|---|---|
| AGENTS.md | Root (+ per app later); pointers only |
| find-skills | Before inventing workflows |
| behuman | All markdown |
| Module names | kebab-case under `apps/` |
| Docs | Planning at root/`docs/`; app ADRs under `apps/<name>/docs/` |
| Health | Never advertise skill brands |

---

## 9. `.gitignore` essentials

```gitignore
.env
.env.*
!.env.example
!**/.env.example
*.p12
*.pem
infra/certs/
**/application-local.yml
target/
.idea/
.DS_Store
```

---

## 10. Success checklist

- [x] `AGENTS.md` + thin `CLAUDE.md`
- [x] `.cursor/commands|rules|agents` scaffold
- [x] find-skills installed at `~/.cursor/skills/find-skills/`
- [x] Placeholder `apps/` / `libs/` / `infra/` / `archive/` / `docs/`
- [x] `.env.example` + gitignore secret defaults
- [x] Legacy migrate into `archive/legacy-portfolio/`
- [x] Placeholder README per `apps/*` (status = not started)
- [ ] Each shipped app: thin `AGENTS.md` + `README.md` + `.env.example` + real `pom.xml`
