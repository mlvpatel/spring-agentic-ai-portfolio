package com.portfolio.shared.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Offline-first assist helper. Uses Spring AI {@link ChatClient} only when a {@link ChatModel}
 * bean is present and {@code OPENAI_API_KEY} (or {@code spring.ai.openai.api-key}) is set.
 * A live call waits at most {@link #DEFAULT_CALL_TIMEOUT} (override with
 * {@code portfolio.ai.call-timeout-ms}). Timeout or any thrown failure returns the offline string.
 * Default reactor tests stay green with no paid key.
 */
public class PortfolioAiClient {

    static final Duration DEFAULT_CALL_TIMEOUT = Duration.ofSeconds(20);

    private static final ExecutorService CALLS = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task, "portfolio-ai-call");
        thread.setDaemon(true);
        return thread;
    });

    @FunctionalInterface
    interface LiveCall {
        String complete(String system, String user) throws Exception;
    }

    private final LiveCall liveCall;
    private final boolean live;
    private final Duration callTimeout;

    public PortfolioAiClient(ObjectProvider<ChatModel> chatModels, Environment environment) {
        this(buildLiveCall(chatModels, environment), readTimeout(environment));
    }

    PortfolioAiClient(LiveCall liveCall, Duration callTimeout) {
        this.liveCall = liveCall;
        this.live = liveCall != null;
        this.callTimeout = callTimeout == null || callTimeout.isZero() || callTimeout.isNegative()
                ? DEFAULT_CALL_TIMEOUT
                : callTimeout;
    }

    public boolean isLive() {
        return live;
    }

    Duration callTimeout() {
        return callTimeout;
    }

    /**
     * Returns a short assist string for {@code purpose}. Offline path is deterministic and
     * never calls a remote model. A live call that times out or throws returns that same string.
     */
    public String assist(String purpose, String context) {
        String safePurpose = purpose == null ? "step" : purpose.strip();
        String safeContext = context == null ? "" : context.strip();
        if (!live) {
            return offline(safePurpose);
        }
        String clipped = safeContext.length() > 1500 ? safeContext.substring(0, 1500) : safeContext;
        String system = "You assist one portfolio " + safePurpose + " step. Reply in one short paragraph.";
        String user = clipped.isEmpty() ? safePurpose : clipped;
        return callBounded(system, user, safePurpose);
    }

    private String callBounded(String system, String user, String safePurpose) {
        Future<String> pending = CALLS.submit(() -> liveCall.complete(system, user));
        try {
            String content = pending.get(callTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!StringUtils.hasText(content)) {
                return offline(safePurpose);
            }
            return content;
        } catch (TimeoutException ex) {
            pending.cancel(true);
            return offline(safePurpose);
        } catch (InterruptedException ex) {
            pending.cancel(true);
            Thread.currentThread().interrupt();
            return offline(safePurpose);
        } catch (ExecutionException ex) {
            return offline(safePurpose);
        }
    }

    private static String offline(String safePurpose) {
        return "offline-" + safePurpose + ": skipped (no OPENAI_API_KEY / ChatModel)";
    }

    private static LiveCall buildLiveCall(ObjectProvider<ChatModel> chatModels, Environment environment) {
        String key = firstNonBlank(
                environment.getProperty("OPENAI_API_KEY"),
                environment.getProperty("spring.ai.openai.api-key"));
        ChatModel model = chatModels.getIfAvailable();
        if (model == null || !StringUtils.hasText(key)) {
            return null;
        }
        ChatClient chatClient = ChatClient.create(model);
        return (system, user) -> chatClient.prompt().system(system).user(user).call().content();
    }

    private static Duration readTimeout(Environment environment) {
        String raw = environment.getProperty("portfolio.ai.call-timeout-ms");
        if (!StringUtils.hasText(raw)) {
            return DEFAULT_CALL_TIMEOUT;
        }
        try {
            long millis = Long.parseLong(raw.trim());
            if (millis <= 0) {
                return DEFAULT_CALL_TIMEOUT;
            }
            return Duration.ofMillis(millis);
        } catch (NumberFormatException ex) {
            return DEFAULT_CALL_TIMEOUT;
        }
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
