package com.portfolio.edge.ratelimit;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;

/**
 * Fixed-window counter in Redis: up to {@code burstCapacity} requests per second per client key.
 */
public final class RedisRateLimitBucketStore implements RateLimitBucketStore {

    private final int burstCapacity;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;

    public RedisRateLimitBucketStore(String redisUrl, int burstCapacity) {
        this.burstCapacity = burstCapacity;
        this.redisClient = RedisClient.create(redisUrl);
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
    }

    @Override
    public boolean tryConsume(String clientKey) {
        long windowSecond = System.currentTimeMillis() / 1000L;
        String redisKey = "gateway:ratelimit:" + clientKey + ":" + windowSecond;
        Long count = syncCommands.incr(redisKey);
        if (count != null && count == 1L) {
            syncCommands.expire(redisKey, 2);
        }
        return count != null && count <= burstCapacity;
    }

    @PreDestroy
    void shutdown() {
        connection.close();
        redisClient.shutdown();
    }
}
