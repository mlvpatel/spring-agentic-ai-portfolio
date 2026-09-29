package com.portfolio.edge.filter;

import com.portfolio.edge.ratelimit.InMemoryRateLimitBucketStore;
import com.portfolio.edge.web.JsonErrorBodies;
import com.portfolio.edge.ratelimit.RateLimitBucketStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class RateLimitingGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimitingGatewayFilterFactory.Config> {

    private final RateLimitBucketStore bucketStore;

    @Autowired
    public RateLimitingGatewayFilterFactory(RateLimitBucketStore bucketStore) {
        super(Config.class);
        this.bucketStore = bucketStore;
    }

    /**
     * Unit tests: in-memory token bucket with explicit rates (ignores Redis).
     */
    public RateLimitingGatewayFilterFactory(int replenishRate, int burstCapacity) {
        this(new InMemoryRateLimitBucketStore(replenishRate, burstCapacity));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith("/actuator") || path.startsWith("/fallback")) {
                return chain.filter(exchange);
            }

            String clientKey = clientKey(exchange);
            if (!bucketStore.tryConsume(clientKey)) {
                return onRateLimitExceeded(exchange);
            }
            return chain.filter(exchange);
        };
    }

    private static String clientKey(ServerWebExchange exchange) {
        String principal = exchange.getRequest().getHeaders().getFirst("X-Authenticated-User");
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "default-client";
        if (StringUtils.hasText(principal)) {
            return principal.trim() + "|" + ip;
        }
        return ip;
    }

    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("Retry-After", "1");
        byte[] bytes = JsonErrorBodies.tooManyRequests(exchange.getRequest().getURI().getPath());
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
