# Contributing

Thanks for helping improve this portfolio. Small, focused pull requests are easiest to review.

## Prerequisites

- JDK 21+
- Maven Wrapper (`./mvnw`) — no global Maven install required

## Build and test

Offline tests need no API key:

```bash
./mvnw test
```

That runs the reactor unit/integration tests (H2 for the Postgres-backed samples). You do not need OpenAI, Redis, Docker, or compose for the default suite.

To try optional live ChatClient paths locally, set `OPENAI_API_KEY` and `SPRING_AI_MODEL_CHAT=openai`. Leave them unset for the offline defaults documented in the root README.

## Branch and PR workflow

1. Fork the repo (or create a branch if you have write access).
2. Create a branch from `main`, e.g. `fix/gateway-rate-limit` or `docs/clarify-compose`.
3. Keep changes scoped: one concern per PR when practical.
4. Run `./mvnw test` before you open the PR.
5. Open a pull request against `main` and fill in the PR template.

Do not commit `.env` files with real values, keystores (`*.p12`, `*.pem`), or agent/editor harness files (`.cursor/`, personal skill trees, and similar).

## Code of conduct

Participation is covered by [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md).

## Security issues

If you find a vulnerability or a leaked secret, follow [SECURITY.md](./SECURITY.md). Do not open a public issue for a live credential.
