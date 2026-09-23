package com.portfolio.kotlinrag.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security")
class ApiKeyProperties {
    var apiKey: String = ""
}
