package com.portfolio.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-service backend API keys and path aliases used after gateway auth succeeds.
 * Empty values fall back to {@code gateway.security.apiKey} (shared-key mode).
 */
@ConfigurationProperties(prefix = "gateway.downstream")
public class DownstreamCredentialProperties {

    private Map<String, String> keys = new LinkedHashMap<>();
    private Map<String, String> aliases = new LinkedHashMap<>();

    public Map<String, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, String> keys) {
        this.keys = keys != null ? keys : new LinkedHashMap<>();
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases != null ? aliases : new LinkedHashMap<>();
    }
}
