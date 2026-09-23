package com.portfolio.specorch.web;

import com.portfolio.specorch.service.RunStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SpecOrchestratorController {
    private final RunStore store;

    public SpecOrchestratorController(RunStore store) {
        this.store = store;
    }

    @PostMapping("/runs")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(store.create(body == null ? null : body.get("brief")));
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        return ResponseEntity.ok(store.get(id));
    }
}
