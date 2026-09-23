package com.portfolio.designrag.service;

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

    public UiSpecService(DesignCorpus corpus, Environment environment) {
        this.corpus = corpus;
        this.environment = environment;
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
        return Map.of(
                "mode", mode(),
                "refused", false,
                "stack", stack,
                "intent", intent,
                "tokens", tokens,
                "layout", Map.of("type", "single-column", "spacing", "token-driven"),
                "a11y", List.of("color-contrast-from-tokens", "focus-ring", "label-required")
        );
    }

    private String mode() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("pgvector")) {
            return "jdbc-postgres";
        }
        return "jdbc-h2";
    }
}
