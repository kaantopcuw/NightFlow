package com.nightflow.shoppingcartservice.config;

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
import java.util.List;

/**
 * Filter that extracts user information from headers set by the Gateway.
 * Also verifies the internal secret to prevent gateway bypass attacks.
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Value("${security.internal.secret:dev-internal-secret-change-in-production}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Skip filter for actuator and documentation endpoints
        if (path.startsWith("/actuator") || path.startsWith("/api-docs") || path.startsWith("/swagger")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verify internal secret to prevent gateway bypass
        String internalSecret = request.getHeader("X-Internal-Secret");
        if (!expectedSecret.equals(internalSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Direct service access not allowed");
            return;
        }

        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userId != null && userRole != null) {
            // Request from gateway with user context
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // Service-to-service call - create SYSTEM authentication
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"));
            var authentication = new UsernamePasswordAuthenticationToken("SYSTEM", null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

