package com.portfolio.mcpbroker.service;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolCatalog {
    private final ConcurrentHashMap<String, Map<String, Object>> tools = new ConcurrentHashMap<>();
    private final RemoteToolClient remoteToolClient;

    public ToolCatalog(RemoteToolClient remoteToolClient) {
        this.remoteToolClient = remoteToolClient;
    }

    public Map<String, Object> register(String name, String scope, Map<String, Object> schema) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required");
        }
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("scope required");
        }
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("name", name.strip());
        tool.put("scope", scope.strip());
        tool.put("schema", schema == null ? Map.of() : schema);
        tool.put("mode", "offline");
        tools.put(name.strip(), tool);
        return tool;
    }

    public Collection<Map<String, Object>> list() {
        return tools.values();
    }

    public Map<String, Object> invoke(String name, Map<String, Object> args) {
        Map<String, Object> tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("unknown tool");
        }
        if (remoteToolClient.enabled()) {
            Map<String, Object> live = new LinkedHashMap<>(remoteToolClient.invoke(name, args));
            live.put("accepted", true);
            live.put("registeredScope", tool.get("scope"));
            return live;
        }
        return Map.of(
                "mode", "offline",
                "tool", name,
                "accepted", true,
                "result", Map.of(
                        "echoArgs", args == null ? Map.of() : args,
                        "note", "Local stub invocation; set app.mcp.remote-url for live-http."));
    }
}
