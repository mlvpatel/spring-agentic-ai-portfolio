package com.portfolio.harness.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ValidationService {

    private final PortfolioAiClient portfolioAiClient;

    public ValidationService(PortfolioAiClient portfolioAiClient) {
        this.portfolioAiClient = portfolioAiClient;
    }

    public Map<String, Object> handle(Map<String, Object> body) {
        String contract = str(body, "contract");
        String observed = str(body, "observed");
        if (contract.isBlank() || observed.isBlank()) {
            throw new IllegalArgumentException("contract and observed required");
        }
        boolean pass = observed.contains(contract.strip()) || contract.strip().equals(observed.strip());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "offline");
        out.put("verdict", pass ? "PASS" : "FAIL");
        out.put("reason", pass ? "Observed payload matches contract token" : "Observed payload missing contract token");
        out.put("aiNote", portfolioAiClient.assist("validation-note", contract + " | " + observed));
        return out;
    }

    private static String str(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return String.valueOf(body.get(key)).trim();
    }
}
