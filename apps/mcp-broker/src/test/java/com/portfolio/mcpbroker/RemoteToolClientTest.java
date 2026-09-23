package com.portfolio.mcpbroker;

import com.portfolio.mcpbroker.service.RemoteToolClient;
import com.portfolio.mcpbroker.service.ToolCatalog;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteToolClientTest {

    private HttpServer server;
    private String url;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tools/invoke", exchange -> {
            byte[] resp = "{\"ok\":true,\"echo\":\"live\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/tools/invoke";
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Live HTTP invoke hits local stub without paid API key")
    void liveHttpInvoke() {
        RemoteToolClient client = new RemoteToolClient(url);
        ToolCatalog catalog = new ToolCatalog(client);
        catalog.register("ping", "local", Map.of());
        Map<String, Object> result = catalog.invoke("ping", Map.of("x", 1));
        assertThat(result.get("mode")).isEqualTo("live-http");
        assertThat(result.get("status")).isEqualTo(200);
        assertThat(String.valueOf(result.get("body"))).contains("live");
    }
}
