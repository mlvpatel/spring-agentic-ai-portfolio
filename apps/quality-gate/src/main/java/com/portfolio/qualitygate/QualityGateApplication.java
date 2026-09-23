package com.portfolio.qualitygate;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class QualityGateApplication {
    public static void main(String[] args) {
        SpringApplication.run(QualityGateApplication.class, args);
    }
}
