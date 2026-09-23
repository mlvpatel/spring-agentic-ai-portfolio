package com.portfolio.multimodal.web;

import com.portfolio.multimodal.service.TriageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MultimodalController {
    private final TriageService service;
    public MultimodalController(TriageService service) { this.service = service; }

    @PostMapping("/triage")
    public ResponseEntity<Map<String, Object>> triage(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.handle(body));
    }
}
