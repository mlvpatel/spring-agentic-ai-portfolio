package com.portfolio.mcpbroker.web;

import com.portfolio.mcpbroker.service.ToolCatalog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class McpBrokerController {
    private final ToolCatalog catalog;

    public McpBrokerController(ToolCatalog catalog) {
        this.catalog = catalog;
    }

    @PostMapping("/tools")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String name = body == null ? null : String.valueOf(body.getOrDefault("name", ""));
        String scope = body == null ? null : String.valueOf(body.getOrDefault("scope", ""));
        Map<String, Object> schema = body != null && body.get("schema") instanceof Map<?, ?> m
                ? (Map<String, Object>) m : Map.of();
        return ResponseEntity.ok(catalog.register(name, scope, schema));
    }

    @GetMapping("/tools")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(catalog.list());
    }

    @PostMapping("/invoke")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> invoke(@RequestBody Map<String, Object> body) {
        String name = body == null ? null : String.valueOf(body.getOrDefault("name", ""));
        Map<String, Object> args = body != null && body.get("args") instanceof Map<?, ?> m
                ? (Map<String, Object>) m : Map.of();
        return ResponseEntity.ok(catalog.invoke(name, args));
    }
}
