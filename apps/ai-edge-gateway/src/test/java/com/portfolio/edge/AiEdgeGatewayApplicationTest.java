package com.portfolio.edge;

import com.portfolio.edge.filter.ApiKeyAuthGatewayFilterFactory;
import com.portfolio.edge.filter.RateLimitingGatewayFilterFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

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
}
