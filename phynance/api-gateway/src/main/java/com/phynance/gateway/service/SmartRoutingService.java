package com.phynance.gateway.service;

import com.phynance.gateway.config.ApiProviderConfig;
import com.phynance.gateway.model.ApiHealth;
import com.phynance.gateway.model.DataQualityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Intelligent routing service that selects the best API provider based on multiple factors
 */
@Service
public class SmartRoutingService {
    
    private static final Logger log = LoggerFactory.getLogger(SmartRoutingService.class);
    
    private final ApiProviderConfig apiProviderConfig;
    private final ApiHealthMonitoringService healthMonitoringService;
    private final Map<String, ApiHealth> apiHealthMap;
    private final Map<String, Double> apiStrengths;
    private final Map<String, Set<String>> geographicCoverage;
    
    @Autowired
    public SmartRoutingService(ApiProviderConfig apiProviderConfig, 
                             @Lazy ApiHealthMonitoringService healthMonitoringService) {
        this.apiProviderConfig = apiProviderConfig;
        this.healthMonitoringService = healthMonitoringService;
        this.apiHealthMap = new ConcurrentHashMap<>();
        this.apiStrengths = initializeApiStrengths();
        this.geographicCoverage = initializeGeographicCoverage();
    }
    
    /**
     * Select the best API provider for a given request
     */
    public String selectBestProvider(String symbol, String dataType, String region) {
        List<String> availableProviders = getAvailableProviders();
        
        if (availableProviders.isEmpty()) {
            log.warn("No available providers for request: symbol={}, dataType={}", symbol, dataType);
            return null;
        }
        
        // Score each provider based on multiple factors
        Map<String, Double> providerScores = new HashMap<>();
        
        for (String provider : availableProviders) {
            double score = calculateProviderScore(provider, symbol, dataType, region);
            providerScores.put(provider, score);
            log.debug("Provider {} score: {}", provider, score);
        }
        
        // Select the provider with the highest score
        String bestProvider = providerScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        log.info("Selected provider {} for symbol={}, dataType={}, region={}", 
                bestProvider, symbol, dataType, region);
        
        return bestProvider;
    }
    
    /**
     * Calculate a comprehensive score for a provider
     */
    private double calculateProviderScore(String provider, String symbol, String dataType, String region) {
        double score = 0.0;
        
        // Health and performance (40% weight)
        ApiHealth health = apiHealthMap.get(provider);
        if (health != null) {
            double healthScore = calculateHealthScore(health);
            score += healthScore * 0.4;
        }
        
        // Data type specialization (25% weight)
        double specializationScore = calculateSpecializationScore(provider, dataType);
        score += specializationScore * 0.25;
        
        // Geographic coverage (15% weight)
        double geographicScore = calculateGeographicScore(provider, region);
        score += geographicScore * 0.15;
        
        // Time-based routing (10% weight)
        double timeScore = calculateTimeBasedScore(provider);
        score += timeScore * 0.10;
        
        // Cost effectiveness (10% weight)
        double costScore = calculateCostScore(provider);
        score += costScore * 0.10;
        
        return score;
    }
    
    /**
     * Calculate health and performance score
     */
    private double calculateHealthScore(ApiHealth health) {
        double score = 0.0;
        
        // Success rate (40% of health score)
        score += (health.getSuccessRate() / 100.0) * 0.4;
        
        // Response time (30% of health score)
        double responseTimeScore = Math.max(0, 1.0 - (health.getAverageResponseTimeMs() / 5000.0));
        score += responseTimeScore * 0.3;
        
        // Recent activity (20% of health score)
        if (health.getLastSuccessfulResponse() != null) {
            long minutesSinceLastSuccess = java.time.Duration.between(
                    health.getLastSuccessfulResponse(), 
                    java.time.Instant.now()
            ).toMinutes();
            double recencyScore = Math.max(0, 1.0 - (minutesSinceLastSuccess / 60.0));
            score += recencyScore * 0.2;
        }
        
        // Status bonus (10% of health score)
        if (health.getStatus() == ApiHealth.HealthStatus.HEALTHY) {
            score += 0.1;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * Calculate data type specialization score
     */
    private double calculateSpecializationScore(String provider, String dataType) {
        Double strength = apiStrengths.get(provider + ":" + dataType);
        return strength != null ? strength : 0.5; // Default to neutral score
    }
    
    /**
     * Calculate geographic coverage score
     */
    private double calculateGeographicScore(String provider, String region) {
        Set<String> coverage = geographicCoverage.get(provider);
        if (coverage != null && coverage.contains(region)) {
            return 1.0;
        }
        return 0.5; // Partial coverage
    }
    
    /**
     * Calculate time-based routing score
     */
    private double calculateTimeBasedScore(String provider) {
        LocalTime now = LocalTime.now(ZoneId.of("America/New_York"));
        
        // Peak hours: 9:30 AM - 4:00 PM EST
        boolean isPeakHours = now.isAfter(LocalTime.of(9, 30)) && 
                             now.isBefore(LocalTime.of(16, 0));
        
        // During peak hours, prefer faster APIs
        if (isPeakHours) {
            ApiHealth health = apiHealthMap.get(provider);
            if (health != null && health.isFast()) {
                return 1.0;
            }
            return 0.7;
        } else {
            // Off-peak: prefer cost-effective APIs
            ApiHealth health = apiHealthMap.get(provider);
            if (health != null && health.isCostEffective()) {
                return 1.0;
            }
            return 0.8;
        }
    }
    
    /**
     * Calculate cost effectiveness score
     */
    private double calculateCostScore(String provider) {
        ApiHealth health = apiHealthMap.get(provider);
        if (health != null) {
            double costPerRequest = health.getCostPerRequest();
            // Lower cost = higher score
            return Math.max(0, 1.0 - (costPerRequest * 100)); // Scale to 0-1
        }
        return 0.5; // Unknown cost
    }
    
    /**
     * Get available providers (healthy and enabled)
     */
    private List<String> getAvailableProviders() {
        return apiProviderConfig.getProviders().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .map(Map.Entry::getKey)
                .filter(provider -> {
                    ApiHealth health = apiHealthMap.get(provider);
                    return health != null && health.isHealthy();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Update API health information
     */
    public void updateApiHealth(String provider, ApiHealth health) {
        apiHealthMap.put(provider, health);
    }
    
    /**
     * Get routing recommendations for a request
     */
    public Map<String, Object> getRoutingRecommendations(String symbol, String dataType, String region) {
        List<String> availableProviders = getAvailableProviders();
        Map<String, Object> recommendations = new HashMap<>();
        
        // Get scores for all providers
        Map<String, Double> providerScores = new HashMap<>();
        for (String provider : availableProviders) {
            double score = calculateProviderScore(provider, symbol, dataType, region);
            providerScores.put(provider, score);
        }
        
        // Sort providers by score
        List<Map.Entry<String, Double>> sortedProviders = providerScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        recommendations.put("recommendedProvider", sortedProviders.isEmpty() ? null : sortedProviders.get(0).getKey());
        recommendations.put("providerScores", providerScores);
        recommendations.put("availableProviders", availableProviders);
        recommendations.put("totalProviders", availableProviders.size());
        
        return recommendations;
    }
    
    /**
     * Initialize API strengths for different data types
     */
    private Map<String, Double> initializeApiStrengths() {
        Map<String, Double> strengths = new HashMap<>();
        
        // Yahoo Finance strengths
        strengths.put("yahoo-finance:price", 0.9);
        strengths.put("yahoo-finance:fundamentals", 0.8);
        strengths.put("yahoo-finance:news", 0.7);
        strengths.put("yahoo-finance:options", 0.6);
        
        // Alpha Vantage strengths
        strengths.put("alpha-vantage:technical", 0.9);
        strengths.put("alpha-vantage:fundamentals", 0.8);
        strengths.put("alpha-vantage:news", 0.6);
        strengths.put("alpha-vantage:price", 0.7);
        
        // Twelve Data strengths
        strengths.put("twelve-data:real-time", 0.9);
        strengths.put("twelve-data:technical", 0.8);
        strengths.put("twelve-data:price", 0.8);
        strengths.put("twelve-data:fundamentals", 0.6);
        
        // Polygon strengths
        strengths.put("polygon:real-time", 0.95);
        strengths.put("polygon:historical", 0.9);
        strengths.put("polygon:options", 0.8);
        strengths.put("polygon:fundamentals", 0.7);
        
        return strengths;
    }
    
    /**
     * Initialize geographic coverage for each provider
     */
    private Map<String, Set<String>> initializeGeographicCoverage() {
        Map<String, Set<String>> coverage = new HashMap<>();
        
        // Yahoo Finance - Global coverage
        coverage.put("yahoo-finance", Set.of("US", "EU", "ASIA", "GLOBAL"));
        
        // Alpha Vantage - Primarily US and major markets
        coverage.put("alpha-vantage", Set.of("US", "EU", "ASIA"));
        
        // Twelve Data - Global coverage
        coverage.put("twelve-data", Set.of("US", "EU", "ASIA", "GLOBAL"));
        
        // Polygon - Primarily US markets
        coverage.put("polygon", Set.of("US"));
        
        return coverage;
    }
} 