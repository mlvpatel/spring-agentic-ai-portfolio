package com.portfolio.designrag;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class DesignRagStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesignRagStudioApplication.class, args);
    }
}
