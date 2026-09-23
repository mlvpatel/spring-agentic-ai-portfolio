package com.portfolio.paperlab.web;

import com.portfolio.paperlab.service.PaperLabService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PaperLabController {
    private final PaperLabService service;

    public PaperLabController(PaperLabService service) {
        this.service = service;
    }

    @PostMapping("/papers")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.ingest(
                body == null ? null : body.get("title"),
                body == null ? null : body.get("text")));
    }

    @PostMapping("/synthesize")
    public ResponseEntity<Map<String, Object>> synthesize(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.synthesize(body == null ? null : body.get("topic")));
    }
}
