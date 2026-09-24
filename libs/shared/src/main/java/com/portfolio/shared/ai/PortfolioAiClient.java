package com.portfolio.shared.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Offline-first assist helper. Uses Spring AI {@link ChatClient} only when a {@link ChatModel}
 * bean is present and {@code OPENAI_API_KEY} (or {@code spring.ai.openai.api-key}) is set.
 * Default reactor tests stay green with no paid key.
 */
public class PortfolioAiClient {

    private final ChatClient chatClient;
    private final boolean live;

    public PortfolioAiClient(ObjectProvider<ChatModel> chatModels, Environment environment) {
        String key = firstNonBlank(
                environment.getProperty("OPENAI_API_KEY"),
                environment.getProperty("spring.ai.openai.api-key"));
        ChatModel model = chatModels.getIfAvailable();
        if (model != null && StringUtils.hasText(key)) {
            this.chatClient = ChatClient.create(model);
            this.live = true;
        } else {
            this.chatClient = null;
            this.live = false;
        }
    }

    public boolean isLive() {
        return live;
    }

    /**
     * Returns a short assist string for {@code purpose}. Offline path is deterministic and
     * never calls a remote model.
     */
    public String assist(String purpose, String context) {
        String safePurpose = purpose == null ? "step" : purpose.strip();
        String safeContext = context == null ? "" : context.strip();
        if (!live) {
            return "offline-" + safePurpose + ": skipped (no OPENAI_API_KEY / ChatModel)";
        }
        String clipped = safeContext.length() > 1500 ? safeContext.substring(0, 1500) : safeContext;
        return chatClient.prompt()
                .system("You assist one portfolio " + safePurpose + " step. Reply in one short paragraph.")
                .user(clipped.isEmpty() ? safePurpose : clipped)
                .call()
                .content();
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
