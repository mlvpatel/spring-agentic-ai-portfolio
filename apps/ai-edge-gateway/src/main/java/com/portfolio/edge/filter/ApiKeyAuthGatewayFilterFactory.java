package com.portfolio.edge.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Edge API-key filter. Accepts only the configured key via X-API-Key or Bearer.
 * Does not treat JWT-looking {@code ey*} tokens or {@code valid-test-token} as bypasses.
 */
@Component
public class ApiKeyAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiKeyAuthGatewayFilterFactory.Config> {

    private final String configuredApiKey;

    public ApiKeyAuthGatewayFilterFactory(@Value("${gateway.security.apiKey:}") String configuredApiKey) {
        super(Config.class);
        if (!StringUtils.hasText(configuredApiKey)) {
            throw new IllegalStateException(
                    "gateway.security.apiKey must be set (GATEWAY_SECURITY_APIKEY or GATEWAY_API_KEY). Refusing empty secret.");
        }
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith("/actuator") || path.startsWith("/fallback")) {
                return chain.filter(exchange);
            }

            HttpHeaders headers = exchange.getRequest().getHeaders();
            String apiKeyHeader = headers.getFirst("X-API-Key");
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            boolean authorized = false;
            String principal = "anonymous";

            if (apiKeyHeader != null && apiKeyHeader.equals(configuredApiKey)) {
                authorized = true;
                principal = "api-client";
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                if (token.equals(configuredApiKey)) {
                    authorized = true;
                    principal = "bearer-client";
                }
            }

            if (!authorized) {
                return onError(exchange, "Missing or invalid API Key / Authorization Bearer token", HttpStatus.UNAUTHORIZED);
            }

            final String authenticatedUser = principal;
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.header("X-Authenticated-User", authenticatedUser))
                    .build();
            return chain.filter(mutated);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format(
                "{\"status\":%d,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                status.value(), message, exchange.getRequest().getURI().getPath());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
