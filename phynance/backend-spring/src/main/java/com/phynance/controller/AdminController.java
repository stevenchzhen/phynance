package com.phynance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin controller for model configuration and performance monitoring.
 * 
 * ADMIN: All access plus model configuration and performance monitoring
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;
    
    // Mock storage for model configurations and performance metrics
    private final Map<String, Object> modelConfigs = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    /**
     * Get system performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<?> getPerformanceMetrics() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_PERFORMANCE_REQUEST", clientIp, null);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("activeUsers", getActiveUsersCount());
        metrics.put("apiRequestsPerMinute", getApiRequestsPerMinute());
        metrics.put("modelCalculationsPerHour", getModelCalculationsPerHour());
        metrics.put("systemHealth", "HEALTHY");
        metrics.put("memoryUsage", "65%");
        metrics.put("cpuUsage", "45%");
        
        auditService.logMethodAccess(username, "getPerformanceMetrics", "AdminController", "SUCCESS");
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get model performance analytics
     */
    @GetMapping("/model-analytics")
    public ResponseEntity<?> getModelAnalytics() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_MODEL_ANALYTICS_REQUEST", clientIp, null);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("harmonicOscillator", getModelPerformance("harmonic-oscillator"));
        analytics.put("thermodynamics", getModelPerformance("thermodynamics"));
        analytics.put("wavePhysics", getModelPerformance("wave-physics"));
        analytics.put("overallAccuracy", 0.78);
        analytics.put("totalCalculations", 15420);
        analytics.put("successRate", 0.95);
        
        auditService.logMethodAccess(username, "getModelAnalytics", "AdminController", "SUCCESS");
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Update model configuration
     */
    @PostMapping("/model-config")
    public ResponseEntity<?> updateModelConfig(@RequestBody Map<String, Object> config) {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_MODEL_CONFIG_UPDATE", clientIp, null);
        
        String modelType = (String) config.get("modelType");
        if (modelType == null) {
            auditService.logError(username, "Model type is required", clientIp, null);
            return ResponseEntity.badRequest().body("Model type is required");
        }
        
        modelConfigs.put(modelType, config);
        auditService.logMethodAccess(username, "updateModelConfig", "AdminController", "SUCCESS");
        
        return ResponseEntity.ok(Map.of("message", "Model configuration updated successfully", "modelType", modelType));
    }
    
    /**
     * Get current model configurations
     */
    @GetMapping("/model-config")
    public ResponseEntity<?> getModelConfigs() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_MODEL_CONFIG_REQUEST", clientIp, null);
        auditService.logMethodAccess(username, "getModelConfigs", "AdminController", "SUCCESS");
        
        return ResponseEntity.ok(modelConfigs);
    }
    
    /**
     * Get user activity and access logs
     */
    @GetMapping("/user-activity")
    public ResponseEntity<?> getUserActivity() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_USER_ACTIVITY_REQUEST", clientIp, null);
        
        Map<String, Object> activity = new HashMap<>();
        activity.put("activeUsers", getActiveUsersCount());
        activity.put("recentLogins", getRecentLogins());
        activity.put("failedAttempts", getFailedLoginAttempts());
        activity.put("rateLimitViolations", getRateLimitViolations());
        
        auditService.logMethodAccess(username, "getUserActivity", "AdminController", "SUCCESS");
        return ResponseEntity.ok(activity);
    }
    
    /**
     * Export bulk data (requires ADMIN role)
     */
    @PostMapping("/export-data")
    public ResponseEntity<?> exportBulkData(@RequestBody Map<String, Object> exportRequest) {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "ADMIN_BULK_EXPORT_REQUEST", clientIp, null);
        
        String dataType = (String) exportRequest.get("dataType");
        String symbol = (String) exportRequest.get("symbol");
        
        if (dataType == null || symbol == null) {
            auditService.logError(username, "Data type and symbol are required for export", clientIp, null);
            return ResponseEntity.badRequest().body("Data type and symbol are required");
        }
        
        // Mock export process
        Map<String, Object> exportResult = new HashMap<>();
        exportResult.put("exportId", "EXP_" + System.currentTimeMillis());
        exportResult.put("dataType", dataType);
        exportResult.put("symbol", symbol);
        exportResult.put("records", 1250);
        exportResult.put("status", "COMPLETED");
        exportResult.put("downloadUrl", "/api/v1/admin/download/EXP_" + System.currentTimeMillis());
        
        auditService.logMethodAccess(username, "exportBulkData", "AdminController", "SUCCESS");
        return ResponseEntity.ok(exportResult);
    }
    
    // Helper methods for mock data
    private int getActiveUsersCount() {
        return 42; // Mock value
    }
    
    private int getApiRequestsPerMinute() {
        return 156; // Mock value
    }
    
    private int getModelCalculationsPerHour() {
        return 89; // Mock value
    }
    
    private Map<String, Object> getModelPerformance(String modelType) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("accuracy", 0.75 + Math.random() * 0.2);
        performance.put("calculations", (int)(Math.random() * 1000));
        performance.put("averageResponseTime", 150 + Math.random() * 100);
        performance.put("successRate", 0.92 + Math.random() * 0.08);
        return performance;
    }
    
    private Map<String, Object> getRecentLogins() {
        Map<String, Object> logins = new HashMap<>();
        logins.put("lastHour", 12);
        logins.put("last24Hours", 89);
        logins.put("lastWeek", 567);
        return logins;
    }
    
    private Map<String, Object> getFailedLoginAttempts() {
        Map<String, Object> attempts = new HashMap<>();
        attempts.put("lastHour", 3);
        attempts.put("last24Hours", 15);
        attempts.put("blockedIPs", 2);
        return attempts;
    }
    
    private Map<String, Object> getRateLimitViolations() {
        Map<String, Object> violations = new HashMap<>();
        violations.put("lastHour", 8);
        violations.put("last24Hours", 45);
        violations.put("mostViolatedEndpoint", "/api/v1/analysis/harmonic-oscillator");
        return violations;
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
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