package com.nightflow.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        // Enable context propagation for Micrometer tracing in WebFlux
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
