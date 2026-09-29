package com.portfolio.edge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps an inbound gateway path to the backend API key that should be forwarded.
 * Path aliases come from {@code gateway.downstream.aliases} (YAML override);
 * known route aliases fill gaps when binding is empty or incomplete.
 */
@Component
public class DownstreamCredentialMapper {

    private final Environment environment;
    private final String fallbackApiKey;
    private final Map<String, String> aliases;

    public DownstreamCredentialMapper(
            Environment environment,
            DownstreamCredentialProperties properties,
            @Value("${gateway.security.apiKey:}") String fallbackApiKey) {
        this.environment = environment;
        this.fallbackApiKey = fallbackApiKey == null ? "" : fallbackApiKey;
        this.aliases = new LinkedHashMap<>();
        if (properties.getAliases() != null) {
            this.aliases.putAll(properties.getAliases());
        }
        // Hard guarantees for unique path aliases even if YAML map binding is empty.
        putIfAbsent(this.aliases, "/api/v1/patch", "yagni-copilot");
        putIfAbsent(this.aliases, "/api/v1/gate", "quality-gate");
        putIfAbsent(this.aliases, "/api/v1/runs", "spec-orchestrator");
        putIfAbsent(this.aliases, "/api/v1/trajectories", "agent-observability");
        putIfAbsent(this.aliases, "/api/v1/analyze", "agent-observability");
        putIfAbsent(this.aliases, "/api/v1/tools", "mcp-broker");
        putIfAbsent(this.aliases, "/api/v1/invoke", "mcp-broker");
        putIfAbsent(this.aliases, "/api/v1/blueprints", "app-factory");
        putIfAbsent(this.aliases, "/api/v1/papers", "paper-algorithm-lab");
        putIfAbsent(this.aliases, "/api/v1/synthesize", "paper-algorithm-lab");
        putIfAbsent(this.aliases, "/api/v1/architect", "system-architect");
        putIfAbsent(this.aliases, "/api/v1/triage", "multimodal-support-desk");
        putIfAbsent(this.aliases, "/api/v1/query", "kotlin-rag-microservice");
        putIfAbsent(this.aliases, "/api/v1/validate", "ai-validated-integration-harness");
        putIfAbsent(this.aliases, "/api/v1/review", "security-review-assistant");
        putIfAbsent(this.aliases, "/api/v1/design-rag", "design-rag-studio");
        putIfAbsent(this.aliases, "/api/v1/app-factory", "app-factory");
        putIfAbsent(this.aliases, "/api/v1/kotlin-rag", "kotlin-rag-microservice");
    }

    private static void putIfAbsent(Map<String, String> map, String key, String value) {
        // Prefer YAML (possibly bracket-keyed); only fill when that path is absent.
        for (String existing : map.keySet()) {
            if (normalizeAliasPath(existing).equals(key)) {
                return;
            }
        }
        map.putIfAbsent(key, value);
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
            String aliasPath = normalizeAliasPath(entry.getKey());
            if (normalized.equals(aliasPath) || normalized.startsWith(aliasPath + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String normalizeAliasPath(String key) {
        if (key == null || key.isBlank()) {
            return "/";
        }
        String aliasPath = key.trim();
        // Spring map binding may keep bracket form "[/api/v1/x]" for slash keys.
        if (aliasPath.length() >= 2 && aliasPath.startsWith("[") && aliasPath.endsWith("]")) {
            aliasPath = aliasPath.substring(1, aliasPath.length() - 1);
        }
        return aliasPath.startsWith("/") ? aliasPath : "/" + aliasPath;
    }
}
