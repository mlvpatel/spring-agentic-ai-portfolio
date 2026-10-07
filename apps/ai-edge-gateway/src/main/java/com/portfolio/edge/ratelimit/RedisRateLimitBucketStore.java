package com.portfolio.edge.ratelimit;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.function.LongSupplier;

/**
 * Redis token bucket. {@code burstCapacity} is the cap and {@code replenishRate} is tokens
 * added per second. The balance and its TTL are written in one Lua compare-and-set.
 */
public final class RedisRateLimitBucketStore implements RateLimitBucketStore {

    static final String TOKEN_BUCKET_LUA = """
            local tokens = redis.call('HGET', KEYS[1], 'tokens')
            local ts = redis.call('HGET', KEYS[1], 'ts')
            if tokens == false then
              tokens = ''
            end
            if ts == false then
              ts = ''
            end
            if tokens ~= ARGV[1] or ts ~= ARGV[2] then
              return -1
            end
            redis.call('HSET', KEYS[1], 'tokens', ARGV[3], 'ts', ARGV[4])
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5]))
            return 1
            """;

    private final int replenishRate;
    private final int burstCapacity;
    private final LongSupplier nowMillis;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;
    private final String scriptSha;

    public RedisRateLimitBucketStore(String redisUrl, int replenishRate, int burstCapacity) {
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
        this.nowMillis = System::currentTimeMillis;
        this.redisClient = RedisClient.create(redisUrl);
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
        this.scriptSha = syncCommands.scriptLoad(TOKEN_BUCKET_LUA);
    }

    /** Package-visible for unit tests with an injected RedisCommands mock. */
    RedisRateLimitBucketStore(RedisCommands<String, String> syncCommands, int replenishRate, int burstCapacity,
                               String scriptSha, LongSupplier nowMillis) {
        this.replenishRate = replenishRate;
        this.burstCapacity = burstCapacity;
        this.nowMillis = nowMillis;
        this.redisClient = null;
        this.connection = null;
        this.syncCommands = syncCommands;
        this.scriptSha = scriptSha;
    }

    @Override
    public boolean tryConsume(String clientKey) {
        String redisKey = "gateway:ratelimit:" + clientKey;
        for (int attempt = 0; attempt < 8; attempt++) {
            BucketSnapshot current = read(redisKey);
            Decision decision = tryTake(
                    current.tokens(), current.timestampMillis(), nowMillis.getAsLong(), replenishRate, burstCapacity);
            String expectedTokens = current.rawTokens() == null ? "" : current.rawTokens();
            String expectedTs = current.rawTimestamp() == null ? "" : current.rawTimestamp();
            Long written = syncCommands.evalsha(
                    scriptSha,
                    ScriptOutputType.INTEGER,
                    new String[]{redisKey},
                    expectedTokens,
                    expectedTs,
                    Double.toString(decision.tokens()),
                    Long.toString(decision.timestampMillis()),
                    Integer.toString(ttlSeconds()));
            if (written != null && written == 1L) {
                return decision.allowed();
            }
        }
        return false;
    }

    private int ttlSeconds() {
        int rate = Math.max(replenishRate, 1);
        int window = burstCapacity <= 0 ? 2 : (int) Math.ceil((burstCapacity / (double) rate) * 2d) + 2;
        return Math.max(2, window);
    }

    private BucketSnapshot read(String redisKey) {
        List<KeyValue<String, String>> fields = syncCommands.hmget(redisKey, "tokens", "ts");
        String rawTokens = rawAt(fields, 0);
        String rawTs = rawAt(fields, 1);
        return new BucketSnapshot(rawTokens, rawTs, parseDouble(rawTokens), parseLong(rawTs));
    }

    private static String rawAt(List<KeyValue<String, String>> fields, int index) {
        if (fields == null || fields.size() <= index) {
            return null;
        }
        KeyValue<String, String> field = fields.get(index);
        if (field == null || !field.hasValue() || field.getValue() == null || field.getValue().isBlank()) {
            return null;
        }
        return field.getValue();
    }

    private static Double parseDouble(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    private static Long parseLong(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Burst is the capacity. Refill adds {@code replenishRate} tokens per elapsed second,
     * capped at that capacity.
     */
    static Decision tryTake(Double storedTokens, Long storedTimestampMillis, long nowMillis,
                            int replenishRate, int burstCapacity) {
        double capacity = Math.max(0, burstCapacity);
        double available = storedTokens == null ? capacity : Math.max(0d, storedTokens);
        long last = storedTimestampMillis == null ? nowMillis : storedTimestampMillis;
        double elapsedSeconds = Math.max(0d, (nowMillis - last) / 1000d);
        double filled = Math.min(capacity, available + elapsedSeconds * replenishRate);
        boolean allowed = capacity >= 1d && filled >= 1d;
        double next = allowed ? filled - 1d : filled;
        return new Decision(allowed, next, nowMillis);
    }

    record Decision(boolean allowed, double tokens, long timestampMillis) {
    }

    private record BucketSnapshot(String rawTokens, String rawTimestamp, Double tokens, Long timestampMillis) {
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
