# AI edge gateway design

Portfolio edge entry on Spring Boot 4.1.1 and Spring Cloud Gateway (WebFlux). Authenticates callers, rate-limits by client IP, and routes each portfolio app on a unique path or a `/svc/<app>/` prefix. There is no catch-all `/api/**` route for ambiguous `/api/v1/generate` or `/api/v1/ingest`.

## Goal

Provide one HTTP front door for demo and stress runs: API key auth (optional OIDC JWT), Redis-backed or in-memory rate limits, actuator health without skill branding, and deterministic routing to P1–P14 backends.

## Routing

Two patterns coexist:

1. **Unique aliases** under `/api/v1/...` for well-known operations (patch, gate, runs, query, validate, review, and app-scoped generate/ingest).
2. **Service prefixes** `/svc/<app-name>/**` with rewrite to the downstream path (full app API surface).

Generate and ingest are never exposed as bare `/api/v1/generate` or `/api/v1/ingest`. Use:

| Gateway path | Downstream |
|---|---|
| `/api/v1/design-rag/ingest` | design-rag-studio `/api/v1/ingest` |
| `/api/v1/design-rag/generate` | design-rag-studio `/api/v1/generate` |
| `/api/v1/app-factory/generate` | app-factory `/api/v1/generate` |
| `/api/v1/kotlin-rag/ingest` | kotlin-rag-microservice `/api/v1/ingest` |

Backend base URLs come from `GATEWAY_URL_*` environment variables (see `application.yml`).

## Authentication

Default: shared API key from `GATEWAY_SECURITY_APIKEY` or `GATEWAY_API_KEY`, sent as `X-API-Key` or `Authorization: Bearer <same key>`.

Optional OIDC: when `OIDC_ISSUER_URI` / `gateway.security.oidcIssuerUri` is non-empty, Bearer JWTs validated with `NimbusJwtDecoder` against that issuer are accepted (`X-Authenticated-User: oidc-client`). API key behavior is unchanged. Invalid JWT falls through; missing or invalid credentials return 401. Without OIDC, `ey*` and `valid-test-token` Bearer values are rejected unless they equal the configured API key.

`/actuator/**` and `/fallback/**` skip auth.

## Rate limiting

Per client IP on all other paths. Settings under `gateway.rateLimiter`:

- `replenishRate` and `burstCapacity` drive the in-memory token bucket when Redis is off.
- When `REDIS_URL` / `gateway.rateLimiter.redisUrl` is set, limits use a Redis fixed-window counter (INCR + TTL) with `burstCapacity` requests per second per client key.

429 responses include `Retry-After: 1`.

## Stack

| Item | Choice |
|---|---|
| Java | 21 |
| Boot | 4.1.1 (parent BOM) |
| Spring Cloud | 2025.1.x (parent BOM) |
| Gateway | `spring-cloud-starter-gateway-server-webflux` |
| Rate limit store | In-memory or Lettuce Redis |
| Optional auth | OAuth2 resource server (JWT decoder only) |

## Module layout

```text
apps/ai-edge-gateway/
  pom.xml
  DESIGN.md
  src/main/java/com/portfolio/edge/
    filter/
    ratelimit/
    config/
  src/main/resources/application.yml
  src/test/java/...
```

## Out of scope

- Spring AI on the edge
- mTLS and full production IdP wiring in-repo
- Cloud deploy definitions in this module

## Tests

`AiEdgeGatewayApplicationTest` covers health bypass, auth rejection for missing key and fake JWT/test tokens, valid API key, and in-memory rate limit via `new RateLimitingGatewayFilterFactory(0, 2)`.
