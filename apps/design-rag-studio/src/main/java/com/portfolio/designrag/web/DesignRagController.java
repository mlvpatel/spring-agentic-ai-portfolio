package com.portfolio.designrag.web;

import com.portfolio.designrag.service.DesignCorpus;
import com.portfolio.designrag.service.UiSpecService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DesignRagController {
    private final DesignCorpus corpus;
    private final UiSpecService uiSpecService;

    public DesignRagController(DesignCorpus corpus, UiSpecService uiSpecService) {
        this.corpus = corpus;
        this.uiSpecService = uiSpecService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody Map<String, String> body) {
        String csv = body == null ? "" : body.getOrDefault("csv", "");
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("csv required");
        }
        int added = corpus.ingestCsv(csv);
        return ResponseEntity.ok(Map.of("added", added, "total", corpus.size(), "mode", "jdbc"));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, String> body) {
        String stack = body == null ? "" : body.getOrDefault("stack", "");
        String intent = body == null ? "" : body.getOrDefault("intent", "");
        if (stack.isBlank() || intent.isBlank()) {
            throw new IllegalArgumentException("stack and intent required");
        }
        return ResponseEntity.ok(uiSpecService.generate(stack, intent));
    }
}
