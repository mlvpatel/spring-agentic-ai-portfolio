package com.portfolio.edge.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping({"/fallback", "/fallback/{service}"})
    public Mono<Map<String, Object>> fallback(
            ServerWebExchange exchange,
            @PathVariable(value = "service", required = false) String service) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String name = service == null || service.isBlank() ? "edge-gateway" : service;
        return Mono.just(Map.of(
                "status", "DEGRADED",
                "service", name,
                "message", "Downstream unavailable"));
    }
}
