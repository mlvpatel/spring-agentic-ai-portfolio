package com.portfolio.designrag.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UiSpecService {
    private final DesignCorpus corpus;
    private final Environment environment;
    private final PortfolioAiClient aiClient;

    public UiSpecService(DesignCorpus corpus, Environment environment, PortfolioAiClient aiClient) {
        this.corpus = corpus;
        this.environment = environment;
        this.aiClient = aiClient;
    }

    public Map<String, Object> generate(String stack, String intent) {
        List<TokenDoc> hits = corpus.retrieve(intent, 8);
        if (hits.isEmpty()) {
            return Map.of(
                    "mode", mode(),
                    "refused", true,
                    "reason", "No design tokens ingested; refuse generic defaults.",
                    "stack", stack
            );
        }
        Map<String, Object> tokens = new LinkedHashMap<>();
        for (TokenDoc d : hits) {
            tokens.put(d.name(), Map.of("value", d.value(), "notes", d.notes()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode());
        out.put("refused", false);
        out.put("stack", stack);
        out.put("intent", intent);
        out.put("tokens", tokens);
        out.put("layout", Map.of("type", "single-column", "spacing", "token-driven"));
        out.put("a11y", List.of("color-contrast-from-tokens", "focus-ring", "label-required"));
        out.put("aiNote", aiClient.assist("grounded-spec", "stack=" + stack + " intent=" + intent));
        return out;
    }

    private String mode() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("pgvector")) {
            return "jdbc-postgres";
        }
        return "jdbc-h2";
    }
}
