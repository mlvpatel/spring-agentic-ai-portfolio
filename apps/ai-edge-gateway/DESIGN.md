# AI edge gateway design

P10 — Enterprise AI Edge Platform (gateway + policy). Small working gateway first; full multi-app routing and mTLS come later.

## Goal

Ship a Spring Cloud Gateway edge that authenticates with an API key from the environment, applies a simple in-memory rate limit, exposes actuator health without skill/plugin brand names, and can route to one downstream stub URL.

## In scope

- Java 21 + Spring Boot 3.3.3 (parent BOM) + Spring Cloud Gateway
- API key auth via `X-API-Key` or `Authorization: Bearer <same key>`
- Reject JWT-looking `ey*` Bearer tokens and the string `valid-test-token` unless they equal the configured key (they must not)
- In-memory token-bucket rate limit per client IP
- `/actuator/health` and `/fallback/**` without auth
- One catch-all route to a configurable backend URL
- Unit tests for auth bypass rejection and rate limit
- `.env.example` only (no real secrets)

## Out of scope

- Spring AI / ChatClient on the edge
- Redis rate limiter, OAuth2/OIDC IdP, mTLS
- Full P1–P9 route table
- Cloud or production deploy
- Parent Boot upgrade to 4.x

## Acceptance criteria

1. Module builds with `mvn -pl apps/ai-edge-gateway -am -DskipTests package`
2. Missing or wrong API key → 401 on protected paths
3. Bearer `ey…` and Bearer `valid-test-token` → 401 when not the configured key
4. Valid configured key → filter chain continues
5. Rate limit exhausted → 429 with `Retry-After`
6. Health returns UP and body has no skill/plugin brand strings
7. API key comes from env (`GATEWAY_SECURITY_APIKEY` / `GATEWAY_API_KEY`); empty key fails startup

## Stack pin

| Item | Choice |
|---|---|
| Java | 21 |
| Boot | 3.3.3 (parent; keeps `libs/shared` safe) |
| Spring Cloud | 2023.0.3 (parent BOM) |
| Gateway | `spring-cloud-starter-gateway` |
| Reactor | on parent reactor as `apps/ai-edge-gateway` |
| Spring AI | none on this app |

## Module layout

```text
apps/ai-edge-gateway/
  pom.xml
  DESIGN.md
  SECURITY.md
  IMPROVEMENT.md
  .env.example
  src/main/java/...
  src/test/java/...
```
