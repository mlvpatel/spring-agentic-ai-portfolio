package com.portfolio.edge.filter;

import com.portfolio.edge.config.DownstreamCredentialMapper;
import com.portfolio.edge.web.JsonErrorBodies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Edge API-key filter. Accepts the configured key via X-API-Key or Bearer.
 * When {@code gateway.security.oidcIssuerUri} is set, also accepts Bearer JWTs from that issuer
 * (optional audience check via {@code gateway.security.oidcAudience}).
 * After auth, replaces client credentials with the per-route downstream API key.
 * Does not treat JWT-looking {@code ey*} tokens or {@code valid-test-token} as bypasses unless OIDC validates them.
 */
@Component
public class ApiKeyAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiKeyAuthGatewayFilterFactory.Config> {

    private final String configuredApiKey;
    private final JwtDecoder jwtDecoder;
    private final DownstreamCredentialMapper downstreamCredentialMapper;

    public ApiKeyAuthGatewayFilterFactory(
            @Value("${gateway.security.apiKey:}") String configuredApiKey,
            @Value("${gateway.security.oidcIssuerUri:}") String oidcIssuerUri,
            @Value("${gateway.security.oidcAudience:}") String oidcAudience,
            DownstreamCredentialMapper downstreamCredentialMapper) {
        super(Config.class);
        if (!StringUtils.hasText(configuredApiKey)) {
            throw new IllegalStateException(
                    "gateway.security.apiKey must be set (GATEWAY_SECURITY_APIKEY or GATEWAY_API_KEY). Refusing empty secret.");
        }
        this.configuredApiKey = configuredApiKey;
        this.downstreamCredentialMapper = downstreamCredentialMapper;
        this.jwtDecoder = buildJwtDecoder(oidcIssuerUri, oidcAudience);
    }

    private static JwtDecoder buildJwtDecoder(String oidcIssuerUri, String oidcAudience) {
        if (!StringUtils.hasText(oidcIssuerUri)) {
            return null;
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(oidcIssuerUri.trim()).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(oidcIssuerUri.trim());
        if (StringUtils.hasText(oidcAudience)) {
            String expected = oidcAudience.trim();
            OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
                List<String> audiences = jwt.getAudience();
                if (audiences != null && audiences.contains(expected)) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Required audience is missing", null));
            };
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
        } else {
            decoder.setJwtValidator(withIssuer);
        }
        return decoder;
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

            if (apiKeyHeader != null && constantTimeEquals(apiKeyHeader, configuredApiKey)) {
                return forwardAuthorized(exchange, chain, path, "api-client");
            }
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                if (constantTimeEquals(token, configuredApiKey)) {
                    return forwardAuthorized(exchange, chain, path, "bearer-client");
                }
                if (jwtDecoder != null) {
                    return Mono.fromCallable(() -> {
                                jwtDecoder.decode(token);
                                return true;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(ok -> forwardAuthorized(exchange, chain, path, "oidc-client"))
                            .onErrorResume(JwtException.class,
                                    ignored -> onError(exchange,
                                            "Missing or invalid API Key / Authorization Bearer token",
                                            HttpStatus.UNAUTHORIZED));
                }
            }
            return onError(exchange, "Missing or invalid API Key / Authorization Bearer token", HttpStatus.UNAUTHORIZED);
        };
    }

    private Mono<Void> forwardAuthorized(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
            String path,
            String principal) {
        String downstreamKey = downstreamCredentialMapper.resolveDownstreamApiKey(path);
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set("X-API-Key", downstreamKey);
                    h.remove(HttpHeaders.AUTHORIZATION);
                    h.set("X-Authenticated-User", principal);
                }))
                .build();
        return chain.filter(mutated);
    }

    static boolean constantTimeEquals(String presented, String expected) {
        if (presented == null || expected == null) {
            return false;
        }
        byte[] left = presented.getBytes(StandardCharsets.UTF_8);
        byte[] right = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = JsonErrorBodies.unauthorized(
                status.value(), message, exchange.getRequest().getURI().getPath());
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
