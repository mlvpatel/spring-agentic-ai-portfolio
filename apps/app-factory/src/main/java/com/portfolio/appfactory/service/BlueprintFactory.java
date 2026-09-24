package com.portfolio.appfactory.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BlueprintFactory {
    private final PortfolioAiClient portfolioAiClient;

    public BlueprintFactory(PortfolioAiClient portfolioAiClient) {
        this.portfolioAiClient = portfolioAiClient;
    }

    private static final Map<String, String> BLUEPRINTS = Map.of(
            "crud-api", "Minimal Spring Web CRUD API",
            "rag-chatbot", "Offline RAG chatbot skeleton",
            "mcp-client", "MCP client stub module"
    );

    public Collection<Map<String, String>> list() {
        List<Map<String, String>> out = new ArrayList<>();
        BLUEPRINTS.forEach((k, v) -> out.add(Map.of("id", k, "description", v)));
        return out;
    }

    public Map<String, Object> generate(String blueprintId, String appName) {
        if (blueprintId == null || !BLUEPRINTS.containsKey(blueprintId)) {
            throw new IllegalArgumentException("unknown blueprint");
        }
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("appName required");
        }
        String safe = appName.strip().replaceAll("[^a-zA-Z0-9-]", "-").toLowerCase(Locale.ROOT);
        Map<String, String> files = new LinkedHashMap<>();
        files.put("pom.xml", "<project><artifactId>" + safe
                + "</artifactId><parent><artifactId>spring-boot-starter-parent</artifactId>"
                + "<version>3.3.3</version></parent></project>");
        files.put("src/main/resources/application.yml", "spring:\n  application:\n    name: " + safe + "\n");
        files.put("README.md", "# " + safe + "\n\nGenerated from blueprint " + blueprintId + " (offline).\n");
        files.put("docker-compose.yml", "services:\n  app:\n    image: eclipse-temurin:21-jre\n");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "offline");
        out.put("blueprint", blueprintId);
        out.put("appName", safe);
        out.put("files", files);
        out.put("note", "Structure only; no live ChatClient generation.");
        out.put("aiNote", portfolioAiClient.assist("blueprint-note", blueprintId + " " + safe));
        return out;
    }
}
