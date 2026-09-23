package com.portfolio.kotlinrag

import com.portfolio.shared.error.SharedErrorAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(SharedErrorAutoConfiguration::class)
class KotlinRagApplication

fun main(args: Array<String>) {
    runApplication<KotlinRagApplication>(*args)
}
