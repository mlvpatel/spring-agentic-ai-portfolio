package com.portfolio.edge.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DownstreamCredentialMapperTest {

    @Test
    @DisplayName("Falls back to known route aliases when YAML aliases are empty")
    void fallsBackWhenYamlEmpty() {
        DownstreamCredentialProperties props = new DownstreamCredentialProperties();
        DownstreamCredentialMapper mapper =
                new DownstreamCredentialMapper(new MockEnvironment(), props, "gateway-key");

        assertThat(mapper.resolveServiceId("/api/v1/patch")).isEqualTo("yagni-copilot");
        assertThat(mapper.resolveServiceId("/api/v1/gate")).isEqualTo("quality-gate");
        assertThat(mapper.resolveServiceId("/api/v1/query")).isEqualTo("kotlin-rag-microservice");
    }

    @Test
    @DisplayName("YAML alias overrides the known route default")
    void yamlOverridesDefault() {
        DownstreamCredentialProperties props = new DownstreamCredentialProperties();
        props.getAliases().put("/api/v1/patch", "custom-service");
        DownstreamCredentialMapper mapper =
                new DownstreamCredentialMapper(new MockEnvironment(), props, "gateway-key");

        assertThat(mapper.resolveServiceId("/api/v1/patch")).isEqualTo("custom-service");
        assertThat(mapper.resolveServiceId("/api/v1/gate")).isEqualTo("quality-gate");
    }
}
