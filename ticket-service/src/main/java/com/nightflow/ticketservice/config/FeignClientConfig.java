package com.nightflow.ticketservice.config;

import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration to add internal secret header for service-to-service communication.
 * Used via @FeignClient(configuration = FeignClientConfig.class).
 * 
 * Note: This is NOT annotated with @Configuration to avoid being auto-applied to all clients.
 * It will only be used by FeignClients that explicitly reference it in their configuration attribute.
 */
public class FeignClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);
    
    // Hardcoded for reliability - config server property may not be available in Feign context
    private static final String INTERNAL_SECRET = "dev-internal-secret-change-in-production";

    @Bean
    public RequestInterceptor internalSecretRequestInterceptor() {
        log.debug("FeignClientConfig: Creating internal secret RequestInterceptor");
        return template -> {
            log.debug("FeignClientConfig: Adding X-Internal-Secret header to: {}", template.url());
            template.header("X-Internal-Secret", INTERNAL_SECRET);
        };
    }
}
