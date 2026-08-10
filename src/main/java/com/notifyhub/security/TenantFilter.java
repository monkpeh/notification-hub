package com.notifyhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Set<String> VALID_SCHEMAS = Set.of("public", "dev");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String target = request.getHeader("X-Edit-Target");
        String schema = (target != null && VALID_SCHEMAS.contains(target)) ? target : "public";

        try {
            TenantContext.set(schema);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
