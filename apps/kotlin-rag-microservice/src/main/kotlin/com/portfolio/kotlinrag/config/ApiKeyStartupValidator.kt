package com.portfolio.kotlinrag.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class ApiKeyStartupValidator(private val properties: ApiKeyProperties) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (!StringUtils.hasText(properties.apiKey)) {
            throw IllegalStateException("app.security.api-key (KOTLIN_RAG_API_KEY) must be set to a non-empty value")
        }
    }
}
