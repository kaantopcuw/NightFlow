package com.nightflow.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    
    // Token format validation constants
    private static final int MIN_TOKEN_LENGTH = 20;
    private static final int MAX_TOKEN_LENGTH = 2048;

    private final WebClient webClient;
    
    @Value("${security.internal.secret:dev-internal-secret-change-in-production}")
    private String internalSecret;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/test",
            "/actuator",
            "/swagger-ui",
            "/api-docs",
            "/aggregate"
    );

    public AuthFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://auth-service").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);

        // 1. Public endpoint'ler için auth bypass
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 2. Token yoksa 401 döndür
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logSecurityEvent("AUTH_MISSING_TOKEN", clientIp, path, null);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. Token format validation (Input Validation)
        String token = authHeader.substring(7);
        if (!isValidTokenFormat(token)) {
            logSecurityEvent("AUTH_INVALID_TOKEN_FORMAT", clientIp, path, "Token length: " + token.length());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. Token varsa, sahibini auth-service'den doğrula
        return webClient.get()
                .uri("/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    // 5. User ID, Role ve Internal Secret bilgilerini header'a ekle
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r.headers(headers -> {
                                headers.add("X-User-Id", String.valueOf(response.get("id")));
                                headers.add("X-User-Role", String.valueOf(response.get("role")));
                                headers.add("X-Internal-Secret", internalSecret);
                            }))
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(e -> {
                    logSecurityEvent("AUTH_TOKEN_VALIDATION_FAILED", clientIp, path, e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * Token format validation - malformed token'ları erken reject et
     */
    private boolean isValidTokenFormat(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        // Length check
        if (token.length() < MIN_TOKEN_LENGTH || token.length() > MAX_TOKEN_LENGTH) {
            return false;
        }
        // JWT format: 3 parts separated by dots
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        // Each part should be non-empty and base64-like
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private String getClientIp(ServerWebExchange exchange) {
        // Check X-Forwarded-For for BFF/proxy scenarios
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
    
    /**
     * Security audit logging - failed auth attempts
     */
    private void logSecurityEvent(String eventType, String clientIp, String path, String details) {
        log.warn("SECURITY_AUDIT | event={} | ip={} | path={} | details={}", 
                eventType, clientIp, path, details != null ? details : "");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
