package com.portfolio.edge.filter;

import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitingGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimitingGatewayFilterFactory.Config> {

    private static final int MAX_BUCKETS = 10_000;

    private final int replenishRate;
    private final int burstCapacity;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingGatewayFilterFactory(
            @Value("${gateway.rateLimiter.replenishRate:100}") int replenishRate,
            @Value("${gateway.rateLimiter.burstCapacity:200}") int burstCapacity) {
        super(Config.class);
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
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

            if (buckets.size() > MAX_BUCKETS) {
                buckets.clear();
            }

            TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(burstCapacity, replenishRate));
            if (!bucket.tryConsume()) {
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

    private static final class TokenBucket {
        private final int capacity;
        private final int refillRatePerSecond;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTimestamp;

        TokenBucket(int capacity, int refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long last = lastRefillTimestamp.get();
            long elapsedSeconds = (now - last) / 1000;
            if (elapsedSeconds > 0) {
                int refill = (int) Math.min(Integer.MAX_VALUE, elapsedSeconds * (long) refillRatePerSecond);
                tokens.updateAndGet(current -> Math.min(capacity, current + refill));
                lastRefillTimestamp.set(now);
            }
        }
    }
}
