package com.phynance.gateway.controller;

import com.phynance.gateway.model.ApiHealth;
import com.phynance.gateway.model.DataQualityResult;
import com.phynance.gateway.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced API Gateway Controller with comprehensive monitoring and routing capabilities
 */
@RestController
@RequestMapping("/api/v2/gateway")
@CrossOrigin(origins = "*")
public class EnhancedApiGatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedApiGatewayController.class);
    
    private final SmartRoutingService smartRoutingService;
    private final ApiHealthMonitoringService healthMonitoringService;
    private final DataQualityValidationService dataQualityService;
    private final EnhancedCachingService cachingService;
    
    @Autowired
    public EnhancedApiGatewayController(SmartRoutingService smartRoutingService,
                                      ApiHealthMonitoringService healthMonitoringService,
                                      DataQualityValidationService dataQualityService,
                                      EnhancedCachingService cachingService) {
        this.smartRoutingService = smartRoutingService;
        this.healthMonitoringService = healthMonitoringService;
        this.dataQualityService = dataQualityService;
        this.cachingService = cachingService;
    }
    
    /**
     * Get comprehensive system health overview
     */
    @GetMapping("/health/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveHealth() {
        log.info("Admin: Retrieving comprehensive system health");
        
        Map<String, Object> healthOverview = new HashMap<>();
        
        // System health summary
        healthOverview.put("systemHealth", healthMonitoringService.getSystemHealthSummary());
        
        // Individual API health
        healthOverview.put("apiHealth", healthMonitoringService.getAllApiHealth());
        
        // Cache statistics
        healthOverview.put("cacheStats", cachingService.getCacheStatistics());
        
        // Cost optimization report
        healthOverview.put("costOptimization", healthMonitoringService.getCostOptimizationReport());
        
        return ResponseEntity.ok(healthOverview);
    }
    
    /**
     * Get real-time API performance metrics
     */
    @GetMapping("/metrics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Map<String, ApiHealth> apiHealth = healthMonitoringService.getAllApiHealth();
        
        // Calculate performance metrics
        double averageResponseTime = apiHealth.values().stream()
                .mapToDouble(ApiHealth::getAverageResponseTimeMs)
                .average()
                .orElse(0.0);
        
        double averageSuccessRate = apiHealth.values().stream()
                .mapToDouble(ApiHealth::getSuccessRate)
                .average()
                .orElse(0.0);
        
        long totalRequests = apiHealth.values().stream()
                .mapToLong(ApiHealth::getTotalRequests)
                .sum();
        
        double totalCost = apiHealth.values().stream()
                .mapToDouble(ApiHealth::getTotalCost)
                .sum();
        
        metrics.put("averageResponseTimeMs", averageResponseTime);
        metrics.put("averageSuccessRate", averageSuccessRate);
        metrics.put("totalRequests", totalRequests);
        metrics.put("totalCost", totalCost);
        metrics.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get smart routing recommendations for a request
     */
    @GetMapping("/routing/recommendations")
    public ResponseEntity<Map<String, Object>> getRoutingRecommendations(
            @RequestParam String symbol,
            @RequestParam String dataType,
            @RequestParam(defaultValue = "US") String region) {
        
        log.info("Getting routing recommendations for symbol={}, dataType={}, region={}", 
                symbol, dataType, region);
        
        Map<String, Object> recommendations = smartRoutingService.getRoutingRecommendations(
                symbol, dataType, region);
        
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Get data quality validation for cached data
     */
    @GetMapping("/data-quality/{symbol}")
    public ResponseEntity<DataQualityResult> getDataQuality(
            @PathVariable String symbol,
            @RequestParam String dataType) {
        
        log.info("Validating data quality for symbol={}, dataType={}", symbol, dataType);
        
        // Get cached data
        Object cachedData = cachingService.getCachedData(symbol, dataType);
        
        if (cachedData == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Validate data quality (using mock secondary data for demonstration)
        Map<String, Object> mockSecondaryData = createMockSecondaryData(symbol, dataType);
        
        DataQualityResult result = dataQualityService.validateDataQuality(
                symbol, dataType, (Map<String, Object>) cachedData, mockSecondaryData);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Warm cache for physics model calculations
     */
    @PostMapping("/cache/warm/{symbol}")
    public ResponseEntity<Map<String, Object>> warmCacheForPhysicsModels(
            @PathVariable String symbol) {
        
        log.info("Warming cache for physics models: {}", symbol);
        
        cachingService.warmCacheForPhysicsModels(symbol);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache warming initiated for " + symbol);
        response.put("symbol", symbol);
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get cache statistics and performance
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> stats = cachingService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Clear all caches
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        log.info("Clearing all caches");
        
        cachingService.clearAllCaches();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get cost optimization report
     */
    @GetMapping("/cost-optimization")
    public ResponseEntity<Map<String, Object>> getCostOptimizationReport() {
        Map<String, Object> report = healthMonitoringService.getCostOptimizationReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get detailed API health for a specific provider
     */
    @GetMapping("/health/{provider}")
    public ResponseEntity<ApiHealth> getApiHealth(@PathVariable String provider) {
        ApiHealth health = healthMonitoringService.getApiHealth(provider);
        
        if (health == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Perform manual health check for a provider
     */
    @PostMapping("/health/{provider}/check")
    public ResponseEntity<Map<String, Object>> performHealthCheck(@PathVariable String provider) {
        log.info("Performing manual health check for provider: {}", provider);
        
        healthMonitoringService.performHealthCheck(provider);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Health check completed for " + provider);
        response.put("provider", provider);
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get system dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // System overview
        dashboard.put("systemHealth", healthMonitoringService.getSystemHealthSummary());
        
        // API performance
        dashboard.put("apiPerformance", getPerformanceMetrics().getBody());
        
        // Cache performance
        dashboard.put("cachePerformance", cachingService.getCacheStatistics());
        
        // Cost analysis
        dashboard.put("costAnalysis", healthMonitoringService.getCostOptimizationReport());
        
        // Popular symbols status
        dashboard.put("popularSymbols", getPopularSymbolsStatus());
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Get status of popular symbols
     */
    private Map<String, Object> getPopularSymbolsStatus() {
        Map<String, Object> status = new HashMap<>();
        String[] popularSymbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
        
        for (String symbol : popularSymbols) {
            Map<String, Object> symbolStatus = new HashMap<>();
            
            // Check if data is cached
            Object cachedData = cachingService.getCachedData(symbol, "price");
            symbolStatus.put("cached", cachedData != null);
            
            // Get routing recommendations
            Map<String, Object> recommendations = smartRoutingService.getRoutingRecommendations(
                    symbol, "price", "US");
            symbolStatus.put("recommendedProvider", recommendations.get("recommendedProvider"));
            
            status.put(symbol, symbolStatus);
        }
        
        return status;
    }
    
    /**
     * Create mock secondary data for data quality validation
     */
    private Map<String, Object> createMockSecondaryData(String symbol, String dataType) {
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("timestamp", java.time.Instant.now());
        
        switch (dataType) {
            case "price":
                data.put("price", 150.0 + Math.random() * 50);
                data.put("volume", 1000000L + (long)(Math.random() * 5000000));
                break;
            case "fundamentals":
                data.put("marketCap", 1000000000L + (long)(Math.random() * 5000000000L));
                data.put("peRatio", 15.0 + Math.random() * 20);
                data.put("dividendYield", Math.random() * 5);
                break;
            case "technical":
                data.put("rsi", 30.0 + Math.random() * 40);
                data.put("macd", -2.0 + Math.random() * 4);
                data.put("bollingerUpper", 160.0 + Math.random() * 20);
                data.put("bollingerLower", 140.0 + Math.random() * 20);
                break;
        }
        
        return data;
    }
} 