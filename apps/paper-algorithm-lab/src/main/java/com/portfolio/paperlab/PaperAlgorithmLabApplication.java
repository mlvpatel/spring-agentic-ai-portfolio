package com.portfolio.paperlab;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class PaperAlgorithmLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaperAlgorithmLabApplication.class, args);
    }
}
