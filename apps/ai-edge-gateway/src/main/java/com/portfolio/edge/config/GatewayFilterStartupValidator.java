package com.portfolio.edge.config;

import com.portfolio.edge.filter.ApiKeyAuthGatewayFilterFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Fails fast if gateway default-filters omit ApiKeyAuth (Spring Security is permitAll at the edge).
 */
@Component
public class GatewayFilterStartupValidator {

    private final Environment environment;
    private final ApiKeyAuthGatewayFilterFactory apiKeyAuthGatewayFilterFactory;

    public GatewayFilterStartupValidator(
            Environment environment,
            ApiKeyAuthGatewayFilterFactory apiKeyAuthGatewayFilterFactory) {
        this.environment = environment;
        this.apiKeyAuthGatewayFilterFactory = apiKeyAuthGatewayFilterFactory;
    }

    @PostConstruct
    void assertApiKeyAuthPresent() {
        if (apiKeyAuthGatewayFilterFactory == null) {
            throw new IllegalStateException("ApiKeyAuthGatewayFilterFactory bean is missing");
        }
        List<?> filters = bindFilters("spring.cloud.gateway.server.webflux.default-filters");
        if (filters.isEmpty()) {
            filters = bindFilters("spring.cloud.gateway.default-filters");
        }
        String joined = filters.stream().map(String::valueOf).collect(Collectors.joining(","));
        if (!joined.contains("ApiKeyAuth")) {
            throw new IllegalStateException(
                    "gateway default-filters must include ApiKeyAuth; Spring Security is permitAll at the edge");
        }
    }

    private List<?> bindFilters(String property) {
        return Binder.get(environment)
                .bind(property, Bindable.listOf(Object.class))
                .orElse(List.of());
    }
}
