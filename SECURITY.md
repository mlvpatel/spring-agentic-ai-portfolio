# Security policy

## Supported versions

Security fixes target the current `main` branch of this repository.

## Reporting a vulnerability

Report security issues privately. Do **not** open a public GitHub issue for a live secret, API key, private key, or exploit that could put running deployments at risk.

Preferred path:

1. Open a private [GitHub security advisory](https://github.com/mlvpatel/spring-agentic-ai-portfolio/security/advisories/new) on this repository, or
2. Contact the repository owner ([@mlvpatel](https://github.com/mlvpatel)) through GitHub with enough detail to reproduce.

Include what you found, where (file/path or endpoint), and steps to reproduce. We will acknowledge the report and work on a fix before any public disclosure.

## Secrets in this project

- Use `.env.example` as a template; keep real values in gitignored `.env` files.
- Default tests and offline app paths do not require `OPENAI_API_KEY`.
- Never commit keystores, PEM private keys, or production credentials.
