package com.portfolio.shared.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
