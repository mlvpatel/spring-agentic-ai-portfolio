# MCP broker design

P6 — Register tools with scopes/schemas; invoke local stubs by default.

## Live backends

- Default: offline stub invoke (`mode=offline`).
- Optional live HTTP: set `app.mcp.remote-url` / `MCP_REMOTE_URL` to a local stub URL (no paid key). Covered by `RemoteToolClientTest` using JDK `HttpServer`.
- Full MCP SDK server install is out of scope for default tests.

## Endpoints

POST `/api/v1/tools` · GET `/api/v1/tools` · POST `/api/v1/invoke` · health
