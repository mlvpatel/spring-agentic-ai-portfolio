package com.portfolio.edge.ratelimit;

import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;

/**
 * Fixed-window counter in Redis: up to {@code burstCapacity} requests per second per client key.
 * {@code replenishRate} is not applied in Redis mode (window is 1s / burst only).
 * INCR + EXPIRE run atomically via Lua so keys always receive a TTL.
 */
public final class RedisRateLimitBucketStore implements RateLimitBucketStore {

    private static final String INCR_EXPIRE_LUA = """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
              redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """;

    private final int burstCapacity;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;
    private final String scriptSha;

    public RedisRateLimitBucketStore(String redisUrl, int burstCapacity) {
        this.burstCapacity = burstCapacity;
        this.redisClient = RedisClient.create(redisUrl);
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
        this.scriptSha = syncCommands.scriptLoad(INCR_EXPIRE_LUA);
    }

    /** Package-visible for unit tests with an injected RedisCommands mock/fake. */
    RedisRateLimitBucketStore(RedisCommands<String, String> syncCommands, int burstCapacity, String scriptSha) {
        this.burstCapacity = burstCapacity;
        this.redisClient = null;
        this.connection = null;
        this.syncCommands = syncCommands;
        this.scriptSha = scriptSha;
    }

    @Override
    public boolean tryConsume(String clientKey) {
        long windowSecond = System.currentTimeMillis() / 1000L;
        String redisKey = "gateway:ratelimit:" + clientKey + ":" + windowSecond;
        Long count = syncCommands.evalsha(scriptSha, ScriptOutputType.INTEGER, new String[]{redisKey}, "2");
        return count != null && count <= burstCapacity;
    }

    @PreDestroy
    void shutdown() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }
}
