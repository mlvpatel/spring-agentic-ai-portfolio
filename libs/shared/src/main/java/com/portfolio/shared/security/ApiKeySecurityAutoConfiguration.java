package com.portfolio.shared.security;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ApiKeyProperties.class)
public class ApiKeySecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiKeyAuthFilter.class)
    public ApiKeyAuthFilter apiKeyAuthFilter(ApiKeyProperties properties) {
        return new ApiKeyAuthFilter(properties);
    }

    @Bean
    public ApplicationRunner apiKeyStartupCheck(ApiKeyProperties properties) {
        return args -> {
            String key = properties.getApiKey();
            if (key == null || key.isBlank()) {
                throw new IllegalStateException(
                        "app.security.api-key must be set to a non-empty value");
            }
        };
    }
}
