package com.portfolio.observability.web;

import com.portfolio.observability.service.TrajectoryStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ObservabilityController {
    private final TrajectoryStore store;

    public ObservabilityController(TrajectoryStore store) {
        this.store = store;
    }

    @PostMapping("/trajectories")
    public ResponseEntity<Map<String, Object>> record(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(store.record(
                body == null ? null : body.get("prompt"),
                body == null ? null : body.get("tool"),
                body == null ? null : body.get("outcome")));
    }

    @GetMapping("/trajectories")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(store.list());
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze() {
        return ResponseEntity.ok(store.analyze());
    }
}
