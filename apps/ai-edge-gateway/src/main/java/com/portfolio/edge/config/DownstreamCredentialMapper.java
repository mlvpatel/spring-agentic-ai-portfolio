package com.portfolio.edge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps an inbound gateway path to the backend API key that should be forwarded.
 * CR-01 path aliases are the static fallback. {@code gateway.downstream.aliases}
 * overrides a path when that alias is set.
 */
@Component
public class DownstreamCredentialMapper {

    private static final Map<String, String> FALLBACK_ALIASES = Map.ofEntries(
            Map.entry("/api/v1/patch", "yagni-copilot"),
            Map.entry("/api/v1/gate", "quality-gate"),
            Map.entry("/api/v1/runs", "spec-orchestrator"),
            Map.entry("/api/v1/trajectories", "agent-observability"),
            Map.entry("/api/v1/analyze", "agent-observability"),
            Map.entry("/api/v1/tools", "mcp-broker"),
            Map.entry("/api/v1/invoke", "mcp-broker"),
            Map.entry("/api/v1/blueprints", "app-factory"),
            Map.entry("/api/v1/papers", "paper-algorithm-lab"),
            Map.entry("/api/v1/synthesize", "paper-algorithm-lab"),
            Map.entry("/api/v1/architect", "system-architect"),
            Map.entry("/api/v1/triage", "multimodal-support-desk"),
            Map.entry("/api/v1/query", "kotlin-rag-microservice"),
            Map.entry("/api/v1/validate", "ai-validated-integration-harness"),
            Map.entry("/api/v1/review", "security-review-assistant"),
            Map.entry("/api/v1/design-rag", "design-rag-studio"),
            Map.entry("/api/v1/app-factory", "app-factory"),
            Map.entry("/api/v1/kotlin-rag", "kotlin-rag-microservice"));

    private final Environment environment;
    private final String fallbackApiKey;
    private final Map<String, String> aliases;

    public DownstreamCredentialMapper(
            Environment environment,
            DownstreamCredentialProperties properties,
            @Value("${gateway.security.apiKey:}") String fallbackApiKey) {
        this.environment = environment;
        this.fallbackApiKey = fallbackApiKey == null ? "" : fallbackApiKey;
        this.aliases = new LinkedHashMap<>(FALLBACK_ALIASES);
        if (properties.getAliases() != null) {
            for (Map.Entry<String, String> entry : properties.getAliases().entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()) {
                    continue;
                }
                String path = entry.getKey().trim();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                this.aliases.put(path, entry.getValue());
            }
        }
    }

    public String resolveDownstreamApiKey(String path) {
        String serviceId = resolveServiceId(path);
        if (serviceId == null) {
            return fallbackApiKey;
        }
        String fromProps = null;
        if (environment != null) {
            fromProps = environment.getProperty("gateway.downstream.keys." + serviceId);
        }
        if (StringUtils.hasText(fromProps)) {
            return fromProps.trim();
        }
        return fallbackApiKey;
    }

    String resolveServiceId(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (normalized.startsWith("/svc/")) {
            String rest = normalized.substring("/svc/".length());
            int slash = rest.indexOf('/');
            return slash < 0 ? rest : rest.substring(0, slash);
        }
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            String aliasPath = entry.getKey();
            if (normalized.equals(aliasPath) || normalized.startsWith(aliasPath + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }
}
