# Shared library

Maven artifact `com.portfolio:shared-lib` at `libs/shared/`.

Cross-cutting pieces for the Servlet apps:

- `ApiError` / `GlobalExceptionHandler` (auto-config)
- `PortfolioAiClient` (offline by default; uses Spring AI `ChatClient` only when a `ChatModel` bean exists and `OPENAI_API_KEY` is set)

No API keys in this module. No default Bearer bypasses.

Parent POM: repo root. Legacy modules under `archive/legacy-portfolio/` are off the live reactor.
