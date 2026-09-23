package com.portfolio.qualitygate.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class QualityGateService {
    private final Environment environment;

    public QualityGateService(Environment environment) {
        this.environment = environment;
    }

    public Map<String, Object> evaluate(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source required");
        }
        List<Map<String, String>> findings = new ArrayList<>();
        String[] lines = source.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("TODO") || line.contains("FIXME")) {
                findings.add(Map.of("severity", "MEDIUM", "rule", "todo-left", "line", String.valueOf(i + 1), "msg", "TODO/FIXME present"));
            }
            if (line.toLowerCase(Locale.ROOT).contains("password") && line.contains("=")) {
                findings.add(Map.of("severity", "HIGH", "rule", "hardcoded-secret-pattern", "line", String.valueOf(i + 1), "msg", "Possible hardcoded secret"));
            }
            if (line.contains("System.out.println")) {
                findings.add(Map.of("severity", "LOW", "rule", "stdout-noise", "line", String.valueOf(i + 1), "msg", "Prefer a logger"));
            }
        }
        boolean fail = findings.stream().anyMatch(f -> "HIGH".equals(f.get("severity")));
        String verdict = fail ? "FAIL" : "PASS";
        String mode = Arrays.asList(environment.getActiveProfiles()).contains("ai") ? "ai-optional" : "offline";
        return Map.of(
                "mode", mode,
                "verdict", verdict,
                "findings", findings,
                "explanation", fail
                        ? "Deterministic gate failed on HIGH findings."
                        : "No HIGH findings; LLM explanation skipped in offline mode.");
    }
}
