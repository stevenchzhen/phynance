package com.phynance.gateway.security;

import com.phynance.gateway.service.AuditService;
import com.phynance.gateway.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;
    
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, AuditService auditService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String ipAddress = getClientIpAddress(request);
        final String userAgent = request.getHeader("User-Agent");
        
        // Skip JWT processing for authentication endpoints
        if (isAuthenticationEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in request to: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        
        try {
            username = jwtService.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Log successful API access
                    auditService.logApiAccess(username, request.getRequestURI(), request.getMethod(), ipAddress, 200);
                    
                    logger.debug("JWT authentication successful for user: {} accessing: {}", username, request.getRequestURI());
                } else {
                    logger.warn("JWT validation failed for user: {} accessing: {}", username, request.getRequestURI());
                    auditService.logSecurityEvent(username, "JWT_VALIDATION_FAILED", ipAddress, userAgent);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage());
            auditService.logError("UNKNOWN", "JWT_PROCESSING_ERROR: " + e.getMessage(), ipAddress, userAgent);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isAuthenticationEndpoint(String requestURI) {
        return requestURI.startsWith("/api/v1/auth/") || 
               requestURI.equals("/api/v1/auth") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.equals("/error");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 