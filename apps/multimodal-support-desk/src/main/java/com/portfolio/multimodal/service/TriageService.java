package com.portfolio.multimodal.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TriageService {

    public Map<String, Object> handle(Map<String, Object> body) {
        String ticket = str(body, "ticket");
        String imageMeta = str(body, "imageMeta");
        if (ticket.isBlank()) {
            throw new IllegalArgumentException("ticket required");
        }
        boolean hasImage = !imageMeta.isBlank();
        String severity = ticket.toLowerCase(Locale.ROOT).contains("outage") ? "high" : "medium";
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "offline");
        out.put("severity", severity);
        out.put("hasImageMeta", hasImage);
        out.put("summary", "Triage: " + ticket.strip().lines().findFirst().orElse("").strip());
        out.put("nextSteps", List.of("ack customer", hasImage ? "inspect attached image meta" : "request screenshot if needed"));
        return out;
    }

    private static String str(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return String.valueOf(body.get(key)).trim();
    }
}
