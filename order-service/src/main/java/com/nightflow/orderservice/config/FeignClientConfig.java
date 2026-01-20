package com.nightflow.orderservice.config;

import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration to add internal secret header for service-to-service communication.
 */
public class FeignClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);
    private static final String INTERNAL_SECRET = "dev-internal-secret-change-in-production";

    @Bean
    public RequestInterceptor internalSecretRequestInterceptor() {
        log.debug("FeignClientConfig: Creating internal secret RequestInterceptor");
        return template -> {
            template.header("X-Internal-Secret", INTERNAL_SECRET);
        };
    }
}
