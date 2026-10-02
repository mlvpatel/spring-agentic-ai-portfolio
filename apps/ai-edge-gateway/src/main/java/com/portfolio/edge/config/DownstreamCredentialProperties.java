package com.portfolio.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Path aliases used after gateway auth succeeds.
 * Per-service keys stay in {@code gateway.downstream.keys} and are read from the environment.
 * An empty key falls back to {@code gateway.security.apiKey} (shared-key mode).
 */
@ConfigurationProperties(prefix = "gateway.downstream")
public class DownstreamCredentialProperties {

    private Map<String, String> aliases = new LinkedHashMap<>();

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases != null ? aliases : new LinkedHashMap<>();
    }
}
