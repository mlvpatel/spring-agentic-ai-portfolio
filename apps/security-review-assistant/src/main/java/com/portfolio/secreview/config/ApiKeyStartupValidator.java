package com.portfolio.secreview.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApiKeyStartupValidator implements ApplicationRunner {
    private final ApiKeyProperties properties;
    public ApiKeyStartupValidator(ApiKeyProperties properties) { this.properties = properties; }
    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("app.security.api-key (SECURITY_REVIEW_API_KEY) must be set to a non-empty value");
        }
    }
}
