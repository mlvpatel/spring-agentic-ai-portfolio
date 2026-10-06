# Close-out

Status date: 2026-10-07.

The portfolio code that can be finished here is finished. Public main is [mlvpatel/spring-agentic-ai-portfolio](https://github.com/mlvpatel/spring-agentic-ai-portfolio).

## Done

- Offline portfolio. With `OPENAI_API_KEY` unset, assist calls return a deterministic `offline-...` string. Tests assert that path.
- Tests. `./mvnw -B test` on 2026-10-07, finished 2026-10-07T00:43:18+02:00: BUILD SUCCESS, 118 tests, 0 failures, 0 errors, 0 skipped. Wall time 32.776 s. Maven total time 31.835 s.
- Gateway auth. The edge requires `X-API-Key` or `Authorization: Bearer` equal to `GATEWAY_SECURITY_APIKEY`. JWT-looking `ey*` strings and `valid-test-token` are rejected.
- Concurrency fix. `YagniPatchServiceTest.concurrentProposesDoNotThrow` is in the 118. One `JavaParser` per measure call.
- Stress on 2026-10-06: 17/17 routes, each 50/50 HTTP 200, 0 errors, auth 401/401/401. Window `2026-10-06T02:27:01Z` to `2026-10-06T02:29:48Z`. Counts are in `docs/stress-results.md`.
- Audit passed. `.planning/v1.0-MILESTONE-AUDIT.md` is `status: passed` (5/5). Nyquist is `COMPLIANT`.
- Public repo. Tip before this close-out: `955290be329263f5ef18efef388bc4ab1eec38d3`. CI run [37426834014](https://github.com/mlvpatel/spring-agentic-ai-portfolio/actions/runs/37426834014) completed with conclusion `success` (updated 2026-10-06T07:00:25Z).

## Left

These are the only leftovers. They need you, not more code.

- Live OpenAI call. Checked 2026-10-07: `OPENAI_API_KEY` is absent from the process environment, the login shell, and `infra/compose/.env`. There is no root `.env`. No call was made.
- Image registry push. Checked 2026-10-07: no docker login (`~/.docker/config.json` has `credsStore=desktop` and an empty `auths` map; `docker-credential-desktop list` is empty). The local gateway image was not pushed. No cluster was created.

## Remotes

| Remote | Repo | Role |
|---|---|---|
| `origin` | `mlvpatel/Agentic-AI-Expert-Portfolio` | Private / local history. Left alone. |
| `public` | `mlvpatel/spring-agentic-ai-portfolio` | Publish line. Commits go on top of public main; no force-push. |

Do not force-push public main. Do not publish `.cursor/`, `AGENTS.md`, `CLAUDE.md`, `.env`, `*.p12`, `*.pem`, or `archive/`. Product planning under `.planning/` is part of the public tree.
