package com.portfolio.secreview.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReviewService {

    private final PortfolioAiClient portfolioAiClient;

    public ReviewService(PortfolioAiClient portfolioAiClient) {
        this.portfolioAiClient = portfolioAiClient;
    }

    public Map<String, Object> handle(Map<String, Object> body) {
        String scope = str(body, "scope");
        String artifact = str(body, "artifact");
        if (scope.isBlank() || artifact.isBlank()) {
            throw new IllegalArgumentException("scope and artifact required");
        }
        if (scope.toLowerCase(Locale.ROOT).contains("offensive") || scope.toLowerCase(Locale.ROOT).contains("exploit")) {
            throw new IllegalArgumentException("offensive scopes are refused");
        }
        List<String> findings = new ArrayList<>();
        String lower = artifact.toLowerCase(Locale.ROOT);
        if (lower.contains("password") || lower.contains("api_key") || lower.contains("secret")) {
            findings.add("Possible secret material in artifact text");
        }
        if (!lower.contains("auth")) {
            findings.add("No explicit auth mention in scope artifact");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "offline");
        out.put("scope", scope.strip());
        out.put("findings", findings);
        out.put("status", findings.isEmpty() ? "clean" : "needs-review");
        out.put("note", "Authorized checklist review only; no offensive tooling.");
        out.put("aiNote", portfolioAiClient.assist("review-note", scope + " " + artifact));
        return out;
    }

    private static String str(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return String.valueOf(body.get(key)).trim();
    }
}
