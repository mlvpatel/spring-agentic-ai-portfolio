package com.portfolio.appfactory.web;

import com.portfolio.appfactory.service.BlueprintFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AppFactoryController {
    private final BlueprintFactory factory;

    public AppFactoryController(BlueprintFactory factory) {
        this.factory = factory;
    }

    @GetMapping("/blueprints")
    public ResponseEntity<?> blueprints() {
        return ResponseEntity.ok(factory.list());
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(factory.generate(
                body == null ? null : body.get("blueprint"),
                body == null ? null : body.get("appName")));
    }
}
