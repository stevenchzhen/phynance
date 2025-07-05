package com.phynance.gateway.service;

import com.phynance.gateway.config.ApiProviderConfig;
import com.phynance.gateway.model.ApiHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for monitoring API health and performance metrics
 */
@Service
public class ApiHealthMonitoringService {
    
    private static final Logger log = LoggerFactory.getLogger(ApiHealthMonitoringService.class);
    
    private final ApiProviderConfig apiProviderConfig;
    private final WebClient webClient;
    private final Map<String, ApiHealth> apiHealthMap;
    private final Map<String, AtomicInteger> hourlyRequestCounters;
    private final Map<String, AtomicInteger> dailyRequestCounters;
    
    @Autowired
    public ApiHealthMonitoringService(ApiProviderConfig apiProviderConfig) {
        this.apiProviderConfig = apiProviderConfig;
        this.webClient = WebClient.builder().build();
        this.apiHealthMap = new ConcurrentHashMap<>();
        this.hourlyRequestCounters = new ConcurrentHashMap<>();
        this.dailyRequestCounters = new ConcurrentHashMap<>();
        
        initializeHealthMonitoring();
    }
    
    /**
     * Initialize health monitoring for all configured providers
     */
    private void initializeHealthMonitoring() {
        apiProviderConfig.getProviders().forEach((providerKey, config) -> {
            if (config.isEnabled()) {
                ApiHealth health = new ApiHealth(providerKey);
                apiHealthMap.put(providerKey, health);
                hourlyRequestCounters.put(providerKey, new AtomicInteger(0));
                dailyRequestCounters.put(providerKey, new AtomicInteger(0));
                
                // Set cost per request based on provider
                setProviderCost(providerKey, health);
                
                log.info("Initialized health monitoring for provider: {}", providerKey);
            }
        });
    }
    
    /**
     * Set cost per request for each provider
     */
    private void setProviderCost(String provider, ApiHealth health) {
        switch (provider) {
            case "yahoo-finance":
                health.setCostPerRequest(0.0); // Free tier
                break;
            case "alpha-vantage":
                health.setCostPerRequest(0.0001); // $0.0001 per request
                break;
            case "twelve-data":
                health.setCostPerRequest(0.0002); // $0.0002 per request
                break;
            case "polygon":
                health.setCostPerRequest(0.0005); // $0.0005 per request
                break;
            default:
                health.setCostPerRequest(0.001); // Default cost
        }
    }
    
    /**
     * Perform health check for a specific provider
     */
    public void performHealthCheck(String provider) {
        ApiHealth health = apiHealthMap.get(provider);
        if (health == null) {
            log.warn("No health record found for provider: {}", provider);
            return;
        }
        
        String healthCheckUrl = buildHealthCheckUrl(provider);
        if (healthCheckUrl == null) {
            log.warn("No health check URL configured for provider: {}", provider);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            webClient.get()
                    .uri(healthCheckUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            long responseTime = System.currentTimeMillis() - startTime;
            health.recordSuccessfulRequest(responseTime);
            health.setLastCheckTime(Instant.now());
            
            log.debug("Health check successful for {}: {}ms", provider, responseTime);
            
        } catch (WebClientResponseException e) {
            health.recordFailedRequest("HTTP " + e.getStatusCode() + ": " + e.getStatusText());
            log.warn("Health check failed for {}: HTTP {}", provider, e.getStatusCode());
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                health.recordTimeout();
                log.warn("Health check timeout for {}: {}", provider, e.getMessage());
            } else {
                health.recordFailedRequest(e.getMessage());
                log.warn("Health check error for {}: {}", provider, e.getMessage());
            }
        }
    }
    
    /**
     * Build health check URL for each provider
     */
    private String buildHealthCheckUrl(String provider) {
        ApiProviderConfig.ProviderConfig config = apiProviderConfig.getProviders().get(provider);
        if (config == null) {
            return null;
        }
        
        String baseUrl = config.getBaseUrl();
        
        switch (provider) {
            case "yahoo-finance":
                return baseUrl + "/v8/finance/chart/AAPL?interval=1d&range=1d";
            case "alpha-vantage":
                return baseUrl + "/query?function=TIME_SERIES_DAILY&symbol=AAPL&apikey=DEMO";
            case "twelve-data":
                return baseUrl + "/time_series?symbol=AAPL&interval=1day&apikey=DEMO";
            case "polygon":
                return baseUrl + "/v2/aggs/ticker/AAPL/range/1/day/2023-01-09/2023-01-09?apikey=DEMO";
            default:
                return baseUrl;
        }
    }
    
    /**
     * Record a successful API request
     */
    public void recordSuccessfulRequest(String provider, long responseTime) {
        ApiHealth health = apiHealthMap.get(provider);
        if (health != null) {
            health.recordSuccessfulRequest(responseTime);
            health.incrementHourlyRequests();
            health.incrementDailyRequests();
            health.addCost(health.getCostPerRequest());
            
            // Update counters
            hourlyRequestCounters.get(provider).incrementAndGet();
            dailyRequestCounters.get(provider).incrementAndGet();
        }
    }
    
    /**
     * Record a failed API request
     */
    public void recordFailedRequest(String provider, String error) {
        ApiHealth health = apiHealthMap.get(provider);
        if (health != null) {
            health.recordFailedRequest(error);
            health.incrementHourlyRequests();
            health.incrementDailyRequests();
            
            // Update counters
            hourlyRequestCounters.get(provider).incrementAndGet();
            dailyRequestCounters.get(provider).incrementAndGet();
        }
    }
    
    /**
     * Get health status for all providers
     */
    public Map<String, ApiHealth> getAllApiHealth() {
        return new ConcurrentHashMap<>(apiHealthMap);
    }
    
    /**
     * Get health status for a specific provider
     */
    public ApiHealth getApiHealth(String provider) {
        return apiHealthMap.get(provider);
    }
    
    /**
     * Get overall system health summary
     */
    public Map<String, Object> getSystemHealthSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();
        
        int totalProviders = apiHealthMap.size();
        long healthyProviders = apiHealthMap.values().stream()
                .filter(ApiHealth::isHealthy)
                .count();
        
        double averageResponseTime = apiHealthMap.values().stream()
                .mapToDouble(ApiHealth::getAverageResponseTimeMs)
                .average()
                .orElse(0.0);
        
        double averageSuccessRate = apiHealthMap.values().stream()
                .mapToDouble(ApiHealth::getSuccessRate)
                .average()
                .orElse(0.0);
        
        double totalCost = apiHealthMap.values().stream()
                .mapToDouble(ApiHealth::getTotalCost)
                .sum();
        
        summary.put("totalProviders", totalProviders);
        summary.put("healthyProviders", healthyProviders);
        summary.put("unhealthyProviders", totalProviders - healthyProviders);
        summary.put("healthPercentage", totalProviders > 0 ? (double) healthyProviders / totalProviders * 100 : 0.0);
        summary.put("averageResponseTimeMs", averageResponseTime);
        summary.put("averageSuccessRate", averageSuccessRate);
        summary.put("totalCost", totalCost);
        summary.put("timestamp", Instant.now());
        
        return summary;
    }
    
    /**
     * Get cost optimization report
     */
    public Map<String, Object> getCostOptimizationReport() {
        Map<String, Object> report = new java.util.HashMap<>();
        
        // Find most cost-effective providers
        String mostCostEffective = apiHealthMap.entrySet().stream()
                .filter(entry -> entry.getValue().getTotalRequests() > 0)
                .min(Map.Entry.comparingByValue((a, b) -> Double.compare(a.getCostPerRequest(), b.getCostPerRequest())))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        // Find most expensive providers
        String mostExpensive = apiHealthMap.entrySet().stream()
                .filter(entry -> entry.getValue().getTotalRequests() > 0)
                .max(Map.Entry.comparingByValue((a, b) -> Double.compare(a.getCostPerRequest(), b.getCostPerRequest())))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        // Calculate cost savings potential
        double totalCost = apiHealthMap.values().stream()
                .mapToDouble(ApiHealth::getTotalCost)
                .sum();
        
        double potentialSavings = 0.0;
        if (mostExpensive != null && mostCostEffective != null) {
            ApiHealth expensive = apiHealthMap.get(mostExpensive);
            ApiHealth costEffective = apiHealthMap.get(mostCostEffective);
            potentialSavings = expensive.getTotalRequests() * (expensive.getCostPerRequest() - costEffective.getCostPerRequest());
        }
        
        report.put("mostCostEffectiveProvider", mostCostEffective);
        report.put("mostExpensiveProvider", mostExpensive);
        report.put("totalCost", totalCost);
        report.put("potentialSavings", potentialSavings);
        report.put("costPerProvider", apiHealthMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Map.of(
                                "costPerRequest", entry.getValue().getCostPerRequest(),
                                "totalCost", entry.getValue().getTotalCost(),
                                "totalRequests", entry.getValue().getTotalRequests()
                        )
                )));
        report.put("timestamp", Instant.now());
        
        return report;
    }
    
    /**
     * Scheduled health check for all providers (every 5 minutes)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledHealthCheck() {
        log.info("Performing scheduled health check for all providers");
        apiHealthMap.keySet().forEach(this::performHealthCheck);
    }
    
    /**
     * Reset hourly request counters (every hour)
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void resetHourlyCounters() {
        log.info("Resetting hourly request counters");
        apiHealthMap.values().forEach(ApiHealth::resetHourlyRequests);
        hourlyRequestCounters.values().forEach(counter -> counter.set(0));
    }
    
    /**
     * Reset daily request counters (every day at midnight)
     */
    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    public void resetDailyCounters() {
        log.info("Resetting daily request counters");
        apiHealthMap.values().forEach(ApiHealth::resetDailyRequests);
        dailyRequestCounters.values().forEach(counter -> counter.set(0));
    }
} 