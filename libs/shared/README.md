# Shared library

Maven artifact `com.portfolio:shared-lib` at `libs/shared/`.

Holds cross-cutting Servlet MVC helpers (`ApiError`, `GlobalExceptionHandler`, auto-config). No API keys, no default Bearer bypasses.

Parent POM: repo root. Legacy agent modules that still depend on this artifact live under `archive/legacy-portfolio/` and are not on the live reactor.
