package com.portfolio.mcpbroker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Optional live HTTP tool backend. When {@code app.mcp.remote-url} is set,
 * invoke POSTs JSON to that URL (local stub / compose sidecar). No paid API key required.
 */
@Component
public class RemoteToolClient {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final String remoteUrl;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public RemoteToolClient(@Value("${app.mcp.remote-url:}") String remoteUrl) {
        this(remoteUrl, new ObjectMapper(), timedRestClient());
    }

    RemoteToolClient(String remoteUrl, ObjectMapper objectMapper, RestClient restClient) {
        this.remoteUrl = remoteUrl == null ? "" : remoteUrl.strip();
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    static RestClient timedRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(REQUEST_TIMEOUT);
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    public boolean enabled() {
        return StringUtils.hasText(remoteUrl);
    }

    public Map<String, Object> invoke(String name, Map<String, Object> args) {
        if (!enabled()) {
            throw new IllegalStateException("remote tools disabled");
        }
        try {
            InvokeBody payload = new InvokeBody(name, args == null ? Map.of() : args);
            String body = objectMapper.writeValueAsString(payload);
            ResponseEntity<String> resp = restClient.post()
                    .uri(URI.create(remoteUrl))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("mode", "live-http");
            out.put("tool", name);
            out.put("status", resp.getStatusCode().value());
            out.put("body", resp.getBody());
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("remote invoke failed: " + e.getMessage(), e);
        }
    }

    public record InvokeBody(String name, Map<String, Object> args) {
    }
}
