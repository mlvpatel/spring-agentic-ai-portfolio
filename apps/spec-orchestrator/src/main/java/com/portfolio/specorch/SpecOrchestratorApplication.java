package com.portfolio.specorch;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class SpecOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpecOrchestratorApplication.class, args);
    }
}
