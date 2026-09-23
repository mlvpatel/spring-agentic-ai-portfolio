package com.portfolio.appfactory;

import com.portfolio.shared.error.SharedErrorAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SharedErrorAutoConfiguration.class)
public class AppFactoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppFactoryApplication.class, args);
    }
}
