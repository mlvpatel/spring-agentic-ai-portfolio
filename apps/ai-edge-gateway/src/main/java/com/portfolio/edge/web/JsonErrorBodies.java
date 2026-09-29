package com.portfolio.edge.web;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;

/** JSON error bodies via Jackson (on the gateway classpath). */
public final class JsonErrorBodies {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonErrorBodies() {
    }

    public static byte[] unauthorized(int status, String message, String path) {
        return toBytes(status, "Unauthorized", message, path);
    }

    public static byte[] tooManyRequests(String path) {
        return toBytes(429, "Too Many Requests", "Rate limit exceeded", path);
    }

    private static byte[] toBytes(int status, String error, String message, String path) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("status", status);
        node.put("error", error);
        node.put("message", message == null ? "" : message);
        node.put("path", path == null ? "" : path);
        try {
            return MAPPER.writeValueAsBytes(node);
        } catch (Exception e) {
            return ("{\"status\":" + status + ",\"error\":\"" + error + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }
}
