package com.portfolio.appfactory.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyStartupValidator implements ApplicationRunner {
    private final ApiKeyProperties properties;
    public ApiKeyStartupValidator(ApiKeyProperties properties) { this.properties = properties; }
    @Override
    public void run(ApplicationArguments args) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("app.security.api-key (APP_FACTORY_API_KEY) must be set to a non-empty value");
        }
    }
}
