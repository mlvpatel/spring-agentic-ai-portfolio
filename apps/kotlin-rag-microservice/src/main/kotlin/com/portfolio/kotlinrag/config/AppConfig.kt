package com.portfolio.kotlinrag.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApiKeyProperties::class)
class AppConfig
