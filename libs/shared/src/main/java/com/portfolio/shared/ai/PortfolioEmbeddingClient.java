package com.portfolio.shared.ai;

import com.portfolio.shared.rag.HashingTextEmbedder;
import com.portfolio.shared.rag.TextEmbedder;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Offline hashing embedder unless a Spring AI {@link EmbeddingModel} bean is present
 * and {@code OPENAI_API_KEY} (or {@code spring.ai.openai.api-key}) is set.
 * The starter leaves {@code spring.ai.model.embedding=none}, so unit tests never call the network.
 */
public class PortfolioEmbeddingClient implements TextEmbedder {

    private final HashingTextEmbedder hashing = new HashingTextEmbedder(64);
    private final EmbeddingModel model;
    private final boolean live;

    public PortfolioEmbeddingClient(ObjectProvider<EmbeddingModel> models, Environment environment) {
        String key = firstNonBlank(
                environment.getProperty("OPENAI_API_KEY"),
                environment.getProperty("spring.ai.openai.api-key"));
        EmbeddingModel found = models.getIfAvailable();
        if (found != null && StringUtils.hasText(key)) {
            this.model = found;
            this.live = true;
        } else {
            this.model = null;
            this.live = false;
        }
    }

    public boolean isLive() {
        return live;
    }

    @Override
    public float[] embed(String text) {
        if (!live) {
            return hashing.embed(text);
        }
        return model.embed(text == null ? "" : text);
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a.trim();
        }
        if (StringUtils.hasText(b)) {
            return b.trim();
        }
        return "";
    }
}
