package com.portfolio.shared.ai;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

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
        assertTrue(client.assist("patch-explanation", "ctx").startsWith("offline-"));
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
