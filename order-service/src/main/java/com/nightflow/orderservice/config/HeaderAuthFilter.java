package com.nightflow.orderservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@lombok.extern.slf4j.Slf4j
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Value("${security.internal.secret:dev-internal-secret-change-in-production}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip internal secret check for actuator and api-docs endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = request.getHeader("X-Trace-Id");
        String internalSecret = request.getHeader("X-Internal-Secret");
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");
        
        log.info("[{}] {} {} - userId={}", traceId, request.getMethod(), path, userId);

        // Verify internal secret to prevent gateway bypass attacks
        if (!expectedSecret.equals(internalSecret)) {
            log.error("Auth Failed: Secret mismatch! Received: {}, Expected: {}", internalSecret, expectedSecret);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Direct service access not allowed");
            return;
        }

        if (userId != null && userRole != null) {
            // Request from gateway with user context
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Auth Success: Authenticated as user {}", userId);
        } else {
            // Service-to-service call - create SYSTEM authentication
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "SYSTEM", 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SYSTEM"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Auth Success: Authenticated as SYSTEM");
        }

        filterChain.doFilter(request, response);
    }
}

