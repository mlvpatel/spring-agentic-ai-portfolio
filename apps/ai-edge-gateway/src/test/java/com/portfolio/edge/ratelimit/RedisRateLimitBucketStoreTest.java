package com.portfolio.edge.ratelimit;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisRateLimitBucketStoreTest {

    @Test
    @DisplayName("A slower replenish rate refills fewer tokens than a faster one")
    void replenishRateChangesRefill() {
        AtomicLong slowClock = new AtomicLong(0);
        AtomicLong fastClock = new AtomicLong(0);
        RedisRateLimitBucketStore slow = new RedisRateLimitBucketStore(statefulRedis(), 1, 2, "sha", slowClock::get);
        RedisRateLimitBucketStore fast = new RedisRateLimitBucketStore(statefulRedis(), 10, 2, "sha", fastClock::get);

        assertThat(allowedInARow(slow)).isEqualTo(2);
        assertThat(slow.tryConsume("client")).isFalse();
        slowClock.set(1_000);
        assertThat(slow.tryConsume("client")).isTrue();
        assertThat(slow.tryConsume("client")).isFalse();

        assertThat(allowedInARow(fast)).isEqualTo(2);
        assertThat(fast.tryConsume("client")).isFalse();
        fastClock.set(1_000);
        assertThat(fast.tryConsume("client")).isTrue();
        assertThat(fast.tryConsume("client")).isTrue();
        assertThat(fast.tryConsume("client")).isFalse();
    }

    private static int allowedInARow(RedisRateLimitBucketStore store) {
        int allowed = 0;
        for (int i = 0; i < 5 && store.tryConsume("client"); i++) {
            allowed++;
        }
        return allowed;
    }

    @SuppressWarnings("unchecked")
    private static RedisCommands<String, String> statefulRedis() {
        RedisCommands<String, String> commands = mock(RedisCommands.class);
        Map<String, String[]> buckets = new ConcurrentHashMap<>();
        when(commands.hmget(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String[] state = buckets.get(key);
            if (state == null) {
                return List.of(KeyValue.empty("tokens"), KeyValue.empty("ts"));
            }
            return List.of(KeyValue.just("tokens", state[0]), KeyValue.just("ts", state[1]));
        });
        when(commands.evalsha(anyString(), eq(ScriptOutputType.INTEGER), any(String[].class),
                any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String key = ((String[]) args[2])[0];
            String[] values = scriptValues(args);
            String[] state = buckets.get(key);
            String actualTokens = state == null ? "" : state[0];
            String actualTs = state == null ? "" : state[1];
            if (!actualTokens.equals(values[0]) || !actualTs.equals(values[1])) {
                return -1L;
            }
            buckets.put(key, new String[]{values[2], values[3]});
            return 1L;
        });
        return commands;
    }

    private static String[] scriptValues(Object[] args) {
        if (args.length == 4 && args[3] instanceof String[] packed) {
            return packed;
        }
        String[] values = new String[args.length - 3];
        for (int i = 3; i < args.length; i++) {
            values[i - 3] = String.valueOf(args[i]);
        }
        return values;
    }

    @Test
    @DisplayName("In-memory store keeps buckets for distinct clients")
    void inMemoryKeepsDistinctBuckets() {
        InMemoryRateLimitBucketStore store = new InMemoryRateLimitBucketStore(100, 10);
        assertThat(store.tryConsume("a")).isTrue();
        assertThat(store.tryConsume("b")).isTrue();
        assertThat(store.bucketCount()).isEqualTo(2);
    }
}
