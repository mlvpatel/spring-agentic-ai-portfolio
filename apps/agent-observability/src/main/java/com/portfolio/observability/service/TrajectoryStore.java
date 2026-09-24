package com.portfolio.observability.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class TrajectoryStore {
    private final CopyOnWriteArrayList<Map<String, Object>> trajectories = new CopyOnWriteArrayList<>();
    private final PortfolioAiClient aiClient;

    public TrajectoryStore(PortfolioAiClient aiClient) {
        this.aiClient = aiClient;
    }

    public Map<String, Object> record(String prompt, String tool, String outcome) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt required");
        }
        if (outcome == null || outcome.isBlank()) {
            throw new IllegalArgumentException("outcome required");
        }
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("id", "tr-" + (trajectories.size() + 1));
        t.put("prompt", prompt.strip());
        t.put("tool", tool == null ? "" : tool);
        t.put("outcome", outcome.strip());
        String lower = outcome.toLowerCase(Locale.ROOT);
        t.put("failed", lower.contains("fail") || lower.contains("error"));
        t.put("recordedAt", Instant.now().toString());
        trajectories.add(t);
        return t;
    }

    public List<Map<String, Object>> list() {
        return List.copyOf(trajectories);
    }

    public Map<String, Object> analyze() {
        List<Map<String, Object>> failed = trajectories.stream()
                .filter(t -> Boolean.TRUE.equals(t.get("failed")))
                .toList();
        Map<String, Long> byTool = failed.stream()
                .collect(Collectors.groupingBy(t -> String.valueOf(t.get("tool")), Collectors.counting()));
        List<String> proposals = new ArrayList<>();
        byTool.forEach((tool, count) ->
                proposals.add("Tighten prompt/tool contract for '" + tool + "' (" + count + " failures)"));
        if (proposals.isEmpty()) {
            proposals.add("No failure clusters; keep current prompts.");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "offline");
        out.put("total", trajectories.size());
        out.put("failures", failed.size());
        out.put("clusters", byTool);
        out.put("skillDeltas", proposals);
        out.put("deltaText", aiClient.assist("delta-text", "failures=" + failed.size() + " proposals=" + proposals));
        return out;
    }
}
