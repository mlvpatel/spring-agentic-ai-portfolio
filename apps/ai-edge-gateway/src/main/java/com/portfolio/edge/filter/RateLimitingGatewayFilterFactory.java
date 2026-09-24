package com.portfolio.edge.filter;

import com.portfolio.edge.ratelimit.InMemoryRateLimitBucketStore;
import com.portfolio.edge.ratelimit.RateLimitBucketStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
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

            String clientIp = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "default-client";

            if (!bucketStore.tryConsume(clientIp)) {
                return onRateLimitExceeded(exchange);
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("Retry-After", "1");
        String body = String.format(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\",\"path\":\"%s\"}",
                exchange.getRequest().getURI().getPath());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
