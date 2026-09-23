package com.portfolio.architect.web;

import com.portfolio.architect.service.ArchitectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ArchitectController {
    private final ArchitectService service;

    public ArchitectController(ArchitectService service) {
        this.service = service;
    }

    @PostMapping("/architect")
    public ResponseEntity<Map<String, Object>> architect(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.design(body == null ? null : body.get("requirements")));
    }
}
