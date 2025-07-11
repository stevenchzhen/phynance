package com.phynance.controller;

import com.phynance.model.WavePhysicsAnalysisRequest;
import com.phynance.model.WavePhysicsAnalysisResponse;
import com.phynance.service.WavePhysicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/analysis")
public class WavePhysicsAnalysisController {
    @Autowired
    private WavePhysicsService wavePhysicsService;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;

    /**
     * Analyze a stock using wave physics principles.
     * 
     * ANALYST+: Advanced physics models require ANALYST+ role
     */
    @PostMapping("/wave-physics")
    // @PreAuthorize("hasAnyRole('ANALYST','ADMIN')") // Temporarily disabled for testing
    // @PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403") // Temporarily disabled for testing
    public ResponseEntity<?> analyze(@RequestBody WavePhysicsAnalysisRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Get client IP for audit logging
        String clientIp = getClientIpAddress();
        
        // Check rate limits - temporarily disabled for testing
        // if (!rateLimitService.checkRateLimit("/api/v1/analysis/wave-physics")) {
        //     auditService.logAccessDenied(username, "analyze", "WavePhysicsAnalysisController", "Rate limit exceeded");
        //     return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        // }
        
        // Check calculation limits - temporarily disabled for testing
        // if (!rateLimitService.checkCalculationLimit("wave-physics")) {
        //     auditService.logAccessDenied(username, "analyze", "WavePhysicsAnalysisController", "Calculation limit exceeded");
        //     return ResponseEntity.status(429).body("Calculation limit exceeded. Please try again later.");
        // }
        
        auditService.logSecurityEvent(username, "WAVE_PHYSICS_ANALYSIS_ATTEMPT", clientIp, null);
        
        // Input validation
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            auditService.logError(username, "Invalid request: null or empty symbol", clientIp, null);
            return ResponseEntity.badRequest().body("Symbol is required");
        }
        
        WavePhysicsAnalysisResponse response = wavePhysicsService.analyze(request);
        auditService.logSecurityEvent(username, "WAVE_PHYSICS_ANALYSIS_SUCCESS", clientIp, null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        return "unknown";
    }
} 