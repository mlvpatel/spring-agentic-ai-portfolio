package com.portfolio.harness;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class AiValidatedIntegrationHarnessApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiValidatedIntegrationHarnessApplication.class, args);
    }
}
