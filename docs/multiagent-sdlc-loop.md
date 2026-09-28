# Multi-agent SDLC loop

Stage machine for one Spring AI app at a time: design → develop → build → security → improvement → deploy (dry-run). Orchestrator runs all six stages without pausing for approval between them, stops on the first failed gate, and writes `docs/loop-status.md`.

**BOM pin (live reactor):** parent BOM is Spring Boot **4.1.1**, Spring AI **2.0.1**, Spring Cloud **2025.1.3**, Java 21. Apps under `apps/` and `libs/shared` inherit that parent. This loop does not change those versions.

## Default target

1. `apps/ai-edge-gateway` (P10)
2. Then `apps/yagni-copilot` (P1)
3. Then catalog order in `PROJECT-CATALOG-PROPOSAL.md` section 7

One app per cycle. Do not parallelize apps in one loop run.

## Roles

| Role | Agent file | Command | Job |
|---|---|---|---|
| Orchestrator | `.cursor/agents/sdlc-orchestrator.yml` | `/sdlc-loop` | Run all six stages; stop on fail; update status |
| Designer | `.cursor/agents/sdlc-designer.yml` | `/sdlc-design` | Spec, scope, acceptance criteria |
| Developer | `.cursor/agents/sdlc-developer.yml` | `/sdlc-develop` | Implement features + unit/integration tests |
| Builder | `.cursor/agents/sdlc-builder.yml` | `/sdlc-build` | Package/validate (`mvn package` / module validate) |
| Security tester | `.cursor/agents/sdlc-security-tester.yml` | `/sdlc-security` | Checklist + tests; no secrets in prompts |
| Improver | `.cursor/agents/sdlc-improver.yml` | `/sdlc-improve` | Fix security findings (or record none); keep tests green |
| Deployer | `.cursor/agents/sdlc-deployer.yml` | `/sdlc-deploy` | Local compose or documented dry-run only |

## Stage gates (pass / fail evidence)

| Stage | Pass when | Evidence path / check |
|---|---|---|
| design | Spec exists for the target app | `apps/<app>/DESIGN.md` (or `apps/<app>/docs/DESIGN.md`) with goal, in/out of scope, AC, stack pin |
| develop | Features and tests exist for the module | Source under `apps/<app>/`; tests written; summary in status |
| build | Package succeeds | `mvn -q -DskipTests package` (or `./mvnw -pl apps/<app> -am -DskipTests package`) exit 0 |
| security | Checklist complete, no secrets committed | `apps/<app>/SECURITY.md`; `.env` not committed; only `.env.example`; auth bypass tests where applicable |
| improvement | Findings fixed or explicitly none; tests still green | `apps/<app>/IMPROVEMENT.md`; module tests exit 0 |
| deploy | Dry-run or local compose succeeds | Compose health **or** documented dry-run (compose snippet + green tests OK if container run is impractical). **Never** cloud/prod from this loop |

Fail any gate → orchestrator sets status `BLOCKED`, records the stage, and stops. Do not skip ahead. Do not pause for user approval between stages in one `/sdlc-loop` run.

## Stop conditions

- First gate fail
- User says stop / cancel / pause
- Attempt to write secrets into git, AGENTS.md, or prompts
- Attempt to deploy to a cloud account or production (deploy gate is local/dry-run only)
- Target app path missing with no way to design it
- Scope creep into a second app mid-cycle

## How the user gets pinged

When all six gates pass for the current target:

1. Orchestrator writes `docs/loop-status.md` with `status: COMPLETE`, target app, and a one-line summary.
2. Parent / chat replies with that summary so the human sees the cycle finished.
3. Optional later: user starts `/loop` with `docs/sdlc-loop-prompt.md` for a **manual** recurring check. Do **not** arm a background token-burning loop from setup itself.

## Skills per stage (find-skills first)

Always run find-skills (local `~/.cursor/skills/`, `~/.cursor/plugins/local/`) before inventing a workflow. Apply behuman on all markdown.

| Stage | Prefer (compose freely) |
|---|---|
| design | spec-kit, system-design-primer, ponytail |
| develop | ponytail, agent-skills, gstack (smoke), Context7 for Spring docs |
| build | antigravity-awesome-skills / scaffold packs, ponytail |
| security | agent-skills, authorized security review packs only; never dump secrets |
| improvement | same as develop + security; minimal diffs for checklist fixes |
| deploy | shipping/compose skills if present; local Docker only |

Skills live outside `apps/`. Product `/health` never lists skill or plugin brand names.

## Security rules for the loop

- No API keys, tokens, or private key material in prompts, AGENTS.md, agent YAML, or commands.
- Commit `.env.example` only. `.env` stays gitignored.
- No `.p12` / private `.pem` in git.
- Do not install MCP servers from loop agents.
- Do not delete `archive/legacy-portfolio/`.

## How to run

1. Confirm `docs/loop-status.md` target (default `apps/ai-edge-gateway`).
2. Run `/sdlc-loop` (all six stages in one pass; or invoke `/sdlc-design` … `/sdlc-deploy` one by one).
3. On COMPLETE, read the one-line summary in `docs/loop-status.md`.
4. For the next app, set `target` to `apps/yagni-copilot` and reset status to `IDLE` / stage `design`.

Ready-to-paste prompt for a later manual `/loop`: `docs/sdlc-loop-prompt.md`.
