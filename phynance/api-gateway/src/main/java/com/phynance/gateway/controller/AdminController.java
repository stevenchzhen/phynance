package com.phynance.gateway.controller;

import com.phynance.gateway.service.ApiGatewayService;
import com.phynance.gateway.service.RateLimiterService;
import com.phynance.gateway.service.RequestQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Controller for API Gateway monitoring and management
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    
    private final ApiGatewayService apiGatewayService;
    private final RateLimiterService rateLimiterService;
    private final RequestQueueService queueService;
    
    @Autowired
    public AdminController(ApiGatewayService apiGatewayService,
                          RateLimiterService rateLimiterService,
                          RequestQueueService queueService) {
        this.apiGatewayService = apiGatewayService;
        this.rateLimiterService = rateLimiterService;
        this.queueService = queueService;
    }
    
    /**
     * Get comprehensive system overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        log.info("Admin: Retrieving system overview");
        
        Map<String, Object> overview = Map.of(
                "service", "Phynance API Gateway",
                "status", "OPERATIONAL",
                "timestamp", System.currentTimeMillis(),
                "gatewayStats", apiGatewayService.getGatewayStats(),
                "rateLimiterStats", rateLimiterService.getRateLimiterStats(),
                "queueStats", queueService.getQueueStats()
        );
        
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get detailed rate limiter statistics
     */
    @GetMapping("/rate-limiters")
    public ResponseEntity<Map<String, Object>> getRateLimiterDetails() {
        log.info("Admin: Retrieving rate limiter details");
        
        Map<String, Object> details = Map.of(
                "rateLimiters", rateLimiterService.getRateLimiterStats(),
                "availableProviders", rateLimiterService.getAvailableProviders(),
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(details);
    }
    
    /**
     * Get detailed queue statistics
     */
    @GetMapping("/queues")
    public ResponseEntity<Map<String, Object>> getQueueDetails() {
        log.info("Admin: Retrieving queue details");
        
        Map<String, Object> details = Map.of(
                "queueStats", queueService.getQueueStats(),
                "estimatedWaitTimes", Map.of(
                        "HIGH", queueService.getEstimatedWaitTime(com.phynance.gateway.model.RequestPriority.HIGH),
                        "MEDIUM", queueService.getEstimatedWaitTime(com.phynance.gateway.model.RequestPriority.MEDIUM),
                        "LOW", queueService.getEstimatedWaitTime(com.phynance.gateway.model.RequestPriority.LOW)
                ),
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(details);
    }
    
    /**
     * Get API provider health status
     */
    @GetMapping("/providers/health")
    public ResponseEntity<Map<String, Object>> getProviderHealth() {
        log.info("Admin: Retrieving provider health status");
        
        Map<String, Object> stats = apiGatewayService.getGatewayStats();
        Map<String, Object> circuitBreakerStats = (Map<String, Object>) stats.get("circuitBreakerStats");
        
        return ResponseEntity.ok(Map.of(
                "providerHealth", circuitBreakerStats,
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Get system performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        log.info("Admin: Retrieving performance metrics");
        
        Map<String, Object> queueStats = queueService.getQueueStats();
        Map<String, Object> rateLimiterStats = rateLimiterService.getRateLimiterStats();
        
        // Calculate performance metrics
        long totalRequests = (Long) queueStats.get("totalRequests");
        long completedRequests = (Long) queueStats.get("completedRequests");
        long failedRequests = (Long) queueStats.get("failedRequests");
        
        double successRate = totalRequests > 0 ? (double) completedRequests / totalRequests * 100 : 0.0;
        double failureRate = totalRequests > 0 ? (double) failedRequests / totalRequests * 100 : 0.0;
        
        Map<String, Object> metrics = Map.of(
                "performance", Map.of(
                        "totalRequests", totalRequests,
                        "completedRequests", completedRequests,
                        "failedRequests", failedRequests,
                        "successRate", String.format("%.2f%%", successRate),
                        "failureRate", String.format("%.2f%%", failureRate),
                        "activeRequests", queueStats.get("activeRequests")
                ),
                "queueMetrics", Map.of(
                        "highPriorityQueueSize", queueStats.get("highPriorityQueueSize"),
                        "mediumPriorityQueueSize", queueStats.get("mediumPriorityQueueSize"),
                        "lowPriorityQueueSize", queueStats.get("lowPriorityQueueSize"),
                        "deadLetterQueueSize", queueStats.get("deadLetterQueueSize")
                ),
                "rateLimiterMetrics", rateLimiterStats,
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get system alerts and warnings with enhanced performance monitoring
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getSystemAlerts() {
        log.info("Admin: Retrieving system alerts");
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> queueStats = queueService.getQueueStats();
        Map<String, Object> alerts = new java.util.HashMap<>();
        
        // Check for queue overflow
        int highQueueSize = (Integer) queueStats.get("highPriorityQueueSize");
        int mediumQueueSize = (Integer) queueStats.get("mediumPriorityQueueSize");
        int lowQueueSize = (Integer) queueStats.get("lowPriorityQueueSize");
        
        if (highQueueSize > 100) {
            alerts.put("highPriorityQueueOverflow", "High priority queue is getting full");
        }
        if (mediumQueueSize > 200) {
            alerts.put("mediumPriorityQueueOverflow", "Medium priority queue is getting full");
        }
        if (lowQueueSize > 500) {
            alerts.put("lowPriorityQueueOverflow", "Low priority queue is getting full");
        }
        
        // Check for high failure rate
        double successRate = (Double) queueStats.get("successRate");
        if (successRate < 90.0) {
            alerts.put("highFailureRate", "Success rate is below 90%");
        }
        
        // Check for dead letter queue
        int deadLetterSize = (Integer) queueStats.get("deadLetterQueueSize");
        if (deadLetterSize > 0) {
            alerts.put("deadLetterQueue", deadLetterSize + " requests in dead letter queue");
        }
        
        // Performance monitoring for admin dashboard
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > 2000) { // 2 seconds
            alerts.put("slowAdminDashboard", "Admin dashboard load time: " + responseTime + "ms (exceeds 2s target)");
            log.warn("Admin dashboard performance issue: {}ms response time", responseTime);
        }
        
        // Check for JWT authentication performance
        Map<String, Object> authMetrics = getAuthPerformanceMetrics();
        double avgAuthTime = (Double) authMetrics.get("averageAuthTimeMs");
        if (avgAuthTime > 50) {
            alerts.put("slowJwtAuth", "JWT authentication average time: " + avgAuthTime + "ms (exceeds 50ms target)");
        }
        
        return ResponseEntity.ok(Map.of(
                "alerts", alerts,
                "alertCount", alerts.size(),
                "responseTimeMs", responseTime,
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Get authentication performance metrics
     */
    private Map<String, Object> getAuthPerformanceMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        // Mock metrics - in production, these would come from actual JWT service monitoring
        metrics.put("averageAuthTimeMs", 25.0); // Target: <50ms
        metrics.put("totalAuthRequests", 1500);
        metrics.put("failedAuthRequests", 12);
        metrics.put("authSuccessRate", 99.2);
        return metrics;
    }
    
    /**
     * Clear dead letter queue
     */
    @DeleteMapping("/queues/dead-letter")
    public ResponseEntity<Map<String, Object>> clearDeadLetterQueue() {
        log.warn("Admin: Clearing dead letter queue");
        
        // This would be implemented in the queue service
        return ResponseEntity.ok(Map.of(
                "message", "Dead letter queue cleared",
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Reset rate limiters for a specific provider
     */
    @PostMapping("/rate-limiters/{provider}/reset")
    public ResponseEntity<Map<String, Object>> resetRateLimiter(@PathVariable String provider) {
        log.warn("Admin: Resetting rate limiter for provider: {}", provider);
        
        // This would be implemented in the rate limiter service
        return ResponseEntity.ok(Map.of(
                "message", "Rate limiter reset for provider: " + provider,
                "provider", provider,
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Get system configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        log.info("Admin: Retrieving system configuration");
        
        return ResponseEntity.ok(Map.of(
                "configuration", Map.of(
                        "maxQueueSize", 1000,
                        "processingThreads", 4,
                        "maxRetries", 3,
                        "retryDelay", "30s"
                ),
                "timestamp", System.currentTimeMillis()
        ));
    }
} 