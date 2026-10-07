package com.portfolio.shared.ai;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PortfolioAiClientTest {

    @Test
    void offlineWhenNoKeyAndNoChatModel() {
        Environment env = new MockEnvironment();
        ObjectProvider<ChatModel> empty = new ObjectProvider<>() {
            @Override
            public ChatModel getObject() {
                return null;
            }
        };
        PortfolioAiClient client = new PortfolioAiClient(empty, env);
        assertFalse(client.isLive());
        assertEquals(Duration.ofSeconds(20), client.callTimeout());
        assertTrue(client.assist("patch-explanation", "ctx").startsWith("offline-"));
    }

    @Test
    void liveCallFallsBackWhenItTimesOut() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean started = new AtomicBoolean();
        PortfolioAiClient client = new PortfolioAiClient((system, user) -> {
            started.set(true);
            release.await(5, TimeUnit.SECONDS);
            return "late";
        }, Duration.ofMillis(300));
        try {
            String result = client.assist("patch-explanation", "ctx");
            assertTrue(client.isLive());
            assertTrue(started.get());
            assertTrue(result.startsWith("offline-patch-explanation"));
        } finally {
            release.countDown();
        }
    }

    @Test
    void liveCallFallsBackWhenItThrows() {
        PortfolioAiClient client = new PortfolioAiClient((system, user) -> {
            throw new IllegalStateException("boom");
        }, Duration.ofSeconds(20));
        assertTrue(client.isLive());
        assertTrue(client.assist("patch-explanation", "ctx").startsWith("offline-"));
    }

    @Test
    void liveCallReturnsModelText() {
        PortfolioAiClient client = new PortfolioAiClient((system, user) -> "model-text", Duration.ofSeconds(5));
        assertEquals("model-text", client.assist("patch", "ctx"));
    }

    @Test
    void liveWhenKeyAndChatModelPresent() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("OPENAI_API_KEY", "sk-test-not-real");
        ChatModel model = Mockito.mock(ChatModel.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatModel> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(model);

        PortfolioAiClient client = new PortfolioAiClient(provider, env);
        assertTrue(client.isLive());
    }
}
