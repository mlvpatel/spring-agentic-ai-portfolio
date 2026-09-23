package com.portfolio.qualitygate.web;

import com.portfolio.qualitygate.service.QualityGateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class QualityGateController {
    private final QualityGateService service;

    public QualityGateController(QualityGateService service) {
        this.service = service;
    }

    @PostMapping("/gate")
    public ResponseEntity<Map<String, Object>> gate(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.evaluate(body == null ? null : body.get("source")));
    }
}
