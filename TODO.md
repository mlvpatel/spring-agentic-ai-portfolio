# Implementation plan

Status date: 2026-10-06.

## Done

Public main before this commit is `983f6a3`. That commit has the six cuts, the embedding switch, and a new `JavaParser` per patch request. `./mvnw -B test` on 2026-10-06: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. The GSD audit in `.planning/v1.0-MILESTONE-AUDIT.md` is `passed` (5/5) and Nyquist is `COMPLIANT`.

Earlier stress samples stay in `docs/stress-results.md`. The 2026-10-06 sample in that file covers every route.

## This pass

Three items. An item is done only when this pass records a command result. Cloud cluster deploy stays out. Do not invent a cluster.

### 1. Live OpenAI call

BLOCKED. Checked 2026-10-06: `OPENAI_API_KEY` is absent from the process environment and from the login shell. Gitignored `infra/compose/.env` has no `OPENAI_API_KEY` line. There is no root `.env`. No call was made.

### 2. Re-stress every gateway route

DONE. Docker Desktop 29.6.1 was up. `docker compose -f infra/compose/docker-compose.yml --env-file .env up -d --build`, then `scripts/run-stress-accuracy.sh`, then `docker compose down`. The gitignored `.env` already had a gateway key and per-app keys; they were not printed and not committed. `OPENAI_API_KEY` stayed unset.

Run window: `2026-10-06T02:27:01Z` to `2026-10-06T02:29:48Z`. Seventeen routes, each 50/50 HTTP 200, 0 errors, auth 401/401/401. `POST /api/v1/patch` is the `yagni-copilot` row (p50 53 ms, p95 85 ms) and the `ai-edge-gateway` row (p50 32 ms, p95 45 ms). No HTTP 5xx, so no code change. Counts are in `docs/stress-results.md`.

### 3. Gateway image, no push

DONE for the local build. Command:

`docker build -f infra/docker/Dockerfile.app --build-arg JAR_FILE=apps/ai-edge-gateway/target/ai-edge-gateway-1.0.0-SNAPSHOT.jar -t portfolio/ai-edge-gateway:local .`

Exit 0. Manifest list `sha256:ed1570ccf9161300807e697814ec2e1920b4f3d756a7fb4a4b77ea07406f95d3`. No `docker push`.

Registry publish stays blocked. `docker info` reported an empty username, and `~/.docker/config.json` has `credsStore=desktop` with no registry entries. No cluster was created.

### CI

[37384491586](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/37384491586) completed with conclusion `success`. Workflow CI. Head `983f6a382d7827e7eff1492614a424bea1684234`. Updated `2026-10-05T22:47:34Z`.

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history. Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Commits go on top of public main; no force-push. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, or `archive/`. Product planning under `.planning/` is part of the public tree.
