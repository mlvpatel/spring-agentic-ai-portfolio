package com.portfolio.edge.config;

import com.portfolio.edge.filter.ApiKeyAuthGatewayFilterFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GatewayFilterStartupValidatorTest {

    @Test
    @DisplayName("Starts when ApiKeyAuth is in webflux default-filters")
    void passesWhenApiKeyAuthPresent() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.cloud.gateway.server.webflux.default-filters[0]", "ApiKeyAuth");
        env.setProperty("spring.cloud.gateway.server.webflux.default-filters[1]", "RateLimiting");
        GatewayFilterStartupValidator validator =
                new GatewayFilterStartupValidator(env, mock(ApiKeyAuthGatewayFilterFactory.class));

        assertThatCode(validator::assertApiKeyAuthPresent).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Refuses to start when ApiKeyAuth is missing from default-filters")
    void failsWhenApiKeyAuthMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.cloud.gateway.server.webflux.default-filters[0]", "RateLimiting");
        GatewayFilterStartupValidator validator =
                new GatewayFilterStartupValidator(env, mock(ApiKeyAuthGatewayFilterFactory.class));

        assertThatThrownBy(validator::assertApiKeyAuthPresent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApiKeyAuth");
    }

    @Test
    @DisplayName("Refuses to start when default-filters list is empty")
    void failsWhenFiltersEmpty() {
        MockEnvironment env = new MockEnvironment();
        GatewayFilterStartupValidator validator =
                new GatewayFilterStartupValidator(env, mock(ApiKeyAuthGatewayFilterFactory.class));

        assertThatThrownBy(validator::assertApiKeyAuthPresent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ApiKeyAuth");
    }
}
