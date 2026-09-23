package com.portfolio.secreview.web;

import com.portfolio.secreview.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SecurityReviewController {
    private final ReviewService service;
    public SecurityReviewController(ReviewService service) { this.service = service; }

    @PostMapping("/review")
    public ResponseEntity<Map<String, Object>> review(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.handle(body));
    }
}
