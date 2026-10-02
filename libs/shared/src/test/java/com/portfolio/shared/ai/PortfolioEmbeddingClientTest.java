package com.portfolio.shared.ai;

import com.portfolio.shared.rag.HashingTextEmbedder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortfolioEmbeddingClientTest {

    @Test
    void hashesWhenKeyOrModelMissing() {
        Environment env = new MockEnvironment();
        EmbeddingModel model = Mockito.mock(EmbeddingModel.class);
        PortfolioEmbeddingClient noModel = new PortfolioEmbeddingClient(provider(null), env);
        PortfolioEmbeddingClient noKey = new PortfolioEmbeddingClient(provider(model), env);

        String text = "color.primary #0B1F33 brand navy";
        float[] hashed = new HashingTextEmbedder(64).embed(text);
        assertFalse(noModel.isLive());
        assertFalse(noKey.isLive());
        assertArrayEquals(hashed, noModel.embed(text));
        assertArrayEquals(hashed, noKey.embed(text));
        verify(model, never()).embed(Mockito.anyString());
    }

    @Test
    void usesEmbeddingModelWhenKeyAndBeanPresent() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("OPENAI_API_KEY", "sk-test-not-real");
        EmbeddingModel model = Mockito.mock(EmbeddingModel.class);
        float[] remote = new float[] {0.25f, 0.5f};
        when(model.embed("hello")).thenReturn(remote);

        PortfolioEmbeddingClient client = new PortfolioEmbeddingClient(provider(model), env);

        assertTrue(client.isLive());
        assertArrayEquals(remote, client.embed("hello"));
        verify(model).embed("hello");
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<EmbeddingModel> provider(EmbeddingModel model) {
        ObjectProvider<EmbeddingModel> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(model);
        return provider;
    }
}
