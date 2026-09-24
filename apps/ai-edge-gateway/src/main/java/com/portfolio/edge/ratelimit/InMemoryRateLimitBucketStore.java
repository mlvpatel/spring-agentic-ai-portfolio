package com.portfolio.edge.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-process token bucket per client key (used when Redis is not configured).
 */
public final class InMemoryRateLimitBucketStore implements RateLimitBucketStore {

    private static final int MAX_BUCKETS = 10_000;

    private final int replenishRate;
    private final int burstCapacity;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimitBucketStore(int replenishRate, int burstCapacity) {
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
    }

    @Override
    public boolean tryConsume(String clientKey) {
        if (buckets.size() > MAX_BUCKETS) {
            buckets.clear();
        }
        TokenBucket bucket = buckets.computeIfAbsent(
                clientKey, k -> new TokenBucket(burstCapacity, replenishRate));
        return bucket.tryConsume();
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
