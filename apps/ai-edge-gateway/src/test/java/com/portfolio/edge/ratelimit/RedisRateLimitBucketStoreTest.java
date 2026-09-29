package com.portfolio.edge.ratelimit;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisRateLimitBucketStoreTest {

    @Test
    @DisplayName("Redis store uses evalsha so INCR and EXPIRE stay atomic")
    void usesAtomicEvalSha() {
        @SuppressWarnings("unchecked")
        RedisCommands<String, String> commands = mock(RedisCommands.class);
        AtomicInteger calls = new AtomicInteger();
        when(commands.evalsha(eq("sha-test"), eq(ScriptOutputType.INTEGER), any(String[].class), anyString()))
                .thenAnswer(inv -> (long) calls.incrementAndGet());

        RedisRateLimitBucketStore store = new RedisRateLimitBucketStore(commands, 2, "sha-test");
        assertThat(store.tryConsume("client-a")).isTrue();
        assertThat(store.tryConsume("client-a")).isTrue();
        assertThat(store.tryConsume("client-a")).isFalse();
        assertThat(calls.get()).isEqualTo(3);
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
