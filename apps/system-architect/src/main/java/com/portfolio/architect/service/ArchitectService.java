package com.portfolio.architect.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArchitectService {
    private static final List<String> CORPUS = List.of(
            "Prefer clear service boundaries over shared mutable databases.",
            "Start with vertical slice; add cache only after measured latency need.",
            "Capacity: estimate RPS * p99 latency headroom before sharding."
    );

    public Map<String, Object> design(String requirements) {
        if (requirements == null || requirements.isBlank()) {
            throw new IllegalArgumentException("requirements required");
        }
        String req = requirements.strip();
        String shortReq = req.length() > 40 ? req.substring(0, 40) + "..." : req;
        String mermaid = "C4Context\n"
                + "title System context\n"
                + "Person(user, \"User\")\n"
                + "System(sys, \"Proposed system\", \"" + shortReq + "\")\n"
                + "Rel(user, sys, \"Uses\")\n";
        Map<String, Object> adr = Map.of(
                "title", "ADR-001: Initial decomposition",
                "status", "Proposed",
                "context", req,
                "decision", "Modular monolith first; extract services when team/scale force it.",
                "consequences", List.of("Lower ops cost", "Clear module APIs")
        );
        Map<String, Object> capacity = Map.of(
                "assumptions", "100 RPS peak, 200ms p99 budget",
                "instances", 2,
                "notes", CORPUS.get(2)
        );
        Map<String, Object> workers = Map.of(
                "security", "TLS everywhere; API keys at edge",
                "scalability", CORPUS.get(1),
                "cost", "Defer managed queue until backlog > 1k msgs"
        );
        return Map.of(
                "mode", "offline",
                "requirements", req,
                "c4Mermaid", mermaid,
                "adr", adr,
                "capacity", capacity,
                "parallelReviews", workers,
                "corpusHits", CORPUS);
    }
}
