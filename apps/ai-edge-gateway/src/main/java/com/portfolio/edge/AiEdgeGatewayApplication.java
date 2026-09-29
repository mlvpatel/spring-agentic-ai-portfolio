package com.portfolio.edge;

import com.portfolio.edge.config.DownstreamCredentialProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DownstreamCredentialProperties.class)
public class AiEdgeGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEdgeGatewayApplication.class, args);
    }
}
