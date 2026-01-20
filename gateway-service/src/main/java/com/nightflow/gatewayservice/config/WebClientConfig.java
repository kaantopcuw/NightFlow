package com.nightflow.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @org.springframework.cloud.client.loadbalancer.LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
