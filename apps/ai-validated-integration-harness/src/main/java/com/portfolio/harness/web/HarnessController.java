package com.portfolio.harness.web;

import com.portfolio.harness.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HarnessController {
    private final ValidationService service;
    public HarnessController(ValidationService service) { this.service = service; }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.handle(body));
    }
}
