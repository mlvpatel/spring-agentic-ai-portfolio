package com.portfolio.edge;

import com.portfolio.edge.config.DownstreamCredentialMapper;
import com.portfolio.edge.config.DownstreamCredentialProperties;
import com.portfolio.edge.filter.ApiKeyAuthGatewayFilterFactory;
import com.portfolio.edge.filter.RateLimitingGatewayFilterFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class AiEdgeGatewayApplicationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApiKeyAuthGatewayFilterFactory authFilterFactory;

    @Autowired
    private RateLimitingGatewayFilterFactory rateLimitingFilterFactory;

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        assertThat(webTestClient).isNotNull();
        assertThat(authFilterFactory).isNotNull();
        assertThat(rateLimitingFilterFactory).isNotNull();
    }

    @Test
    @DisplayName("Health bypasses auth and returns UP without skill brands")
    void actuatorHealthShouldBypassAuth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("UP");
                    assertThat(body).doesNotContainIgnoringCase("ponytail");
                    assertThat(body).doesNotContainIgnoringCase("gstack");
                    assertThat(body).doesNotContainIgnoringCase("skill");
                });
    }

    @Test
    @DisplayName("Fallback returns 503")
    void fallbackEndpointsShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DEGRADED")
                .jsonPath("$.service").isEqualTo("edge-gateway");
    }

    @Test
    @DisplayName("Auth rejects missing key")
    void authFilterRejectsUnauthorized() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo").build());
        filter.filter(exchange, ex -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Auth accepts valid X-API-Key")
    void authFilterAcceptsValidApiKey() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .header("X-API-Key", "test-gateway-api-key-for-unit-tests-only")
                        .build());
        boolean[] chainInvoked = {false};
        filter.filter(exchange, mutated -> {
            chainInvoked[0] = true;
            assertThat(mutated.getRequest().getHeaders().getFirst("X-Authenticated-User")).isEqualTo("api-client");
            // No alias match → fallback to gateway key
            assertThat(mutated.getRequest().getHeaders().getFirst("X-API-Key"))
                    .isEqualTo("test-gateway-api-key-for-unit-tests-only");
            assertThat(mutated.getRequest().getHeaders().getFirst("Authorization")).isNull();
            return Mono.empty();
        }).block();
        assertThat(chainInvoked[0]).isTrue();
    }

    @Test
    @DisplayName("Auth remaps X-API-Key to per-service downstream secret")
    void authFilterRemapsDownstreamApiKey() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/patch")
                        .header("X-API-Key", "test-gateway-api-key-for-unit-tests-only")
                        .header("Authorization", "Bearer leftover-client-token")
                        .build());
        boolean[] chainInvoked = {false};
        filter.filter(exchange, mutated -> {
            chainInvoked[0] = true;
            assertThat(mutated.getRequest().getHeaders().getFirst("X-API-Key"))
                    .isEqualTo("test-yagni-downstream-key");
            assertThat(mutated.getRequest().getHeaders().getFirst("Authorization")).isNull();
            assertThat(mutated.getRequest().getHeaders().getFirst("X-Authenticated-User")).isEqualTo("api-client");
            return Mono.empty();
        }).block();
        assertThat(chainInvoked[0]).isTrue();
    }

    @Test
    @DisplayName("Auth accepts Bearer only when token equals configured key")
    void authFilterAcceptsBearerMatchingKey() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .header("Authorization", "Bearer test-gateway-api-key-for-unit-tests-only")
                        .build());
        boolean[] chainInvoked = {false};
        filter.filter(exchange, mutated -> {
            chainInvoked[0] = true;
            return Mono.empty();
        }).block();
        assertThat(chainInvoked[0]).isTrue();
    }

    @Test
    @DisplayName("Auth rejects JWT-looking ey* Bearer bypass")
    void authFilterRejectsEyJwtPrefixBypass() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig")
                        .build());
        filter.filter(exchange, ex -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Auth rejects valid-test-token bypass")
    void authFilterRejectsValidTestTokenBypass() {
        var filter = authFilterFactory.apply(new ApiKeyAuthGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .header("Authorization", "Bearer valid-test-token")
                        .build());
        filter.filter(exchange, ex -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Rate limiter returns 429 when capacity exhausted")
    void rateLimiterEnforcesQuota() {
        RateLimitingGatewayFilterFactory tightLimiter = new RateLimitingGatewayFilterFactory(0, 2);
        var filter = tightLimiter.apply(new RateLimitingGatewayFilterFactory.Config());
        InetSocketAddress remote = new InetSocketAddress("192.168.1.50", 12345);

        MockServerWebExchange ex1 = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo").remoteAddress(remote).build());
        filter.filter(ex1, e -> Mono.empty()).block();
        assertThat(ex1.getResponse().getStatusCode()).isNull();

        MockServerWebExchange ex2 = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo").remoteAddress(remote).build());
        filter.filter(ex2, e -> Mono.empty()).block();
        assertThat(ex2.getResponse().getStatusCode()).isNull();

        MockServerWebExchange ex3 = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo").remoteAddress(remote).build());
        filter.filter(ex3, e -> Mono.empty()).block();
        assertThat(ex3.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(ex3.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("1");
    }

    @Test
    @DisplayName("OIDC subject is the rate-limit identity; fake ey* and valid-test-token stay rejected")
    void oidcSubjectIsRateLimitIdentity() {
        JwtDecoder decoder = token -> {
            if (token.startsWith("eyJ.valid.")) {
                return jwtWithSubject(token.substring("eyJ.valid.".length()));
            }
            throw new JwtException("rejected");
        };
        var auth = oidcFilter(decoder);
        var limiter = new RateLimitingGatewayFilterFactory(0, 1)
                .apply(new RateLimitingGatewayFilterFactory.Config());
        InetSocketAddress remote = new InetSocketAddress("10.1.1.8", 9);

        AtomicReference<String> seen = new AtomicReference<>();
        MockServerWebExchange first = bearerExchange(remote, "eyJ.valid.user-a");
        auth.filter(first, mutated -> {
            seen.set(mutated.getRequest().getHeaders().getFirst("X-Authenticated-User"));
            return limiter.filter(mutated, exchange -> Mono.empty());
        }).block(Duration.ofSeconds(3));
        assertThat(seen.get()).isEqualTo("user-a");
        assertThat(first.getResponse().getStatusCode()).isNull();

        MockServerWebExchange second = bearerExchange(remote, "eyJ.valid.user-a");
        auth.filter(second, mutated -> limiter.filter(mutated, exchange -> Mono.empty()))
                .block(Duration.ofSeconds(3));
        assertThat(second.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        MockServerWebExchange other = bearerExchange(remote, "eyJ.valid.user-b");
        auth.filter(other, mutated -> limiter.filter(mutated, exchange -> Mono.empty()))
                .block(Duration.ofSeconds(3));
        assertThat(other.getResponse().getStatusCode()).isNull();

        MockServerWebExchange fakeEy = bearerExchange(remote, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig");
        auth.filter(fakeEy, exchange -> Mono.empty()).block(Duration.ofSeconds(3));
        assertThat(fakeEy.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        MockServerWebExchange fakeToken = bearerExchange(remote, "valid-test-token");
        auth.filter(fakeToken, exchange -> Mono.empty()).block(Duration.ofSeconds(3));
        assertThat(fakeToken.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Configured API key does not go through the JWT decoder")
    void apiKeySkipsJwtDecoder() {
        AtomicInteger decodes = new AtomicInteger();
        JwtDecoder decoder = token -> {
            decodes.incrementAndGet();
            throw new JwtException("rejected");
        };
        var auth = oidcFilter(decoder);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer configured-gateway-key")
                        .build());
        AtomicReference<String> seen = new AtomicReference<>();
        auth.filter(exchange, mutated -> {
            seen.set(mutated.getRequest().getHeaders().getFirst("X-Authenticated-User"));
            return Mono.empty();
        }).block(Duration.ofSeconds(3));
        assertThat(seen.get()).isEqualTo("bearer-client");
        assertThat(decodes.get()).isZero();
    }

    private static org.springframework.cloud.gateway.filter.GatewayFilter oidcFilter(JwtDecoder decoder) {
        DownstreamCredentialMapper mapper = new DownstreamCredentialMapper(
                new MockEnvironment(), new DownstreamCredentialProperties(), "configured-gateway-key");
        return new ApiKeyAuthGatewayFilterFactory("configured-gateway-key", decoder, mapper)
                .apply(new ApiKeyAuthGatewayFilterFactory.Config());
    }

    private static Jwt jwtWithSubject(String subject) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
    }

    private static MockServerWebExchange bearerExchange(InetSocketAddress remote, String token) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/demo")
                        .remoteAddress(remote)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
    }
}
