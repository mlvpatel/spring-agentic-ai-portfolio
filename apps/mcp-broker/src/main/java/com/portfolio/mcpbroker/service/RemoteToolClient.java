package com.portfolio.mcpbroker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Optional live HTTP tool backend. When {@code app.mcp.remote-url} is set,
 * invoke POSTs JSON to that URL (local stub / compose sidecar). No paid API key required.
 */
@Component
public class RemoteToolClient {
    private final String remoteUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public RemoteToolClient(@Value("${app.mcp.remote-url:}") String remoteUrl) {
        this.remoteUrl = remoteUrl == null ? "" : remoteUrl.strip();
    }

    public boolean enabled() {
        return StringUtils.hasText(remoteUrl);
    }

    public Map<String, Object> invoke(String name, Map<String, Object> args) {
        if (!enabled()) {
            throw new IllegalStateException("remote tools disabled");
        }
        try {
            String body = "{\"name\":\"" + name + "\",\"args\":" + toJson(args) + "}";
            HttpRequest req = HttpRequest.newBuilder(URI.create(remoteUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("mode", "live-http");
            out.put("tool", name);
            out.put("status", resp.statusCode());
            out.put("body", resp.body());
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("remote invoke failed: " + e.getMessage(), e);
        }
    }

    private static String toJson(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : args.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append('"').append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append('"').append(String.valueOf(v).replace("\"", "\\\"")).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
