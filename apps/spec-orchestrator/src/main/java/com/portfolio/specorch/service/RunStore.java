package com.portfolio.specorch.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RunStore {
    private final AtomicLong seq = new AtomicLong();
    private final ConcurrentHashMap<String, Map<String, Object>> runs = new ConcurrentHashMap<>();

    public Map<String, Object> create(String brief) {
        if (brief == null || brief.isBlank()) {
            throw new IllegalArgumentException("brief required");
        }
        String id = "run-" + seq.incrementAndGet();
        List<Map<String, String>> artifacts = List.of(
                Map.of("type", "constitution", "content", "Prefer small diffs and testable acceptance criteria."),
                Map.of("type", "spec", "content", "Feature: " + brief.strip() + "\nAcceptance: happy path + auth failure."),
                Map.of("type", "tasks", "content", "1) API contract\n2) Service logic\n3) Tests"),
                Map.of("type", "routing", "content", "workers=api,data,tests")
        );
        Map<String, Object> run = new LinkedHashMap<>();
        run.put("id", id);
        run.put("brief", brief.strip());
        run.put("status", "COMPLETED");
        run.put("mode", "offline");
        run.put("createdAt", Instant.now().toString());
        run.put("artifacts", artifacts);
        runs.put(id, run);
        return run;
    }

    public Map<String, Object> get(String id) {
        Map<String, Object> run = runs.get(id);
        if (run == null) {
            throw new IllegalArgumentException("unknown run id");
        }
        return run;
    }
}
