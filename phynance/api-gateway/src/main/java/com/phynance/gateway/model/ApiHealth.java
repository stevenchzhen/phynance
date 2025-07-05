package com.phynance.gateway.model;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the health status and performance metrics of an API provider
 */
public class ApiHealth {
    
    private String providerName;
    private HealthStatus status;
    private Instant lastCheckTime;
    private Instant lastSuccessfulResponse;
    private long responseTimeMs;
    private double averageResponseTimeMs;
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private int timeoutCount;
    private double successRate;
    private double costPerRequest;
    private double totalCost;
    private int requestsThisHour;
    private int requestsThisDay;
    private Map<String, Object> performanceMetrics;
    private String errorMessage;
    private boolean isPreferred;
    private double confidenceScore;
    
    public ApiHealth(String providerName) {
        this.providerName = providerName;
        this.status = HealthStatus.UNKNOWN;
        this.lastCheckTime = Instant.now();
        this.performanceMetrics = new ConcurrentHashMap<>();
        this.confidenceScore = 1.0;
    }
    
    // Getters and Setters
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public HealthStatus getStatus() { return status; }
    public void setStatus(HealthStatus status) { this.status = status; }
    
    public Instant getLastCheckTime() { return lastCheckTime; }
    public void setLastCheckTime(Instant lastCheckTime) { this.lastCheckTime = lastCheckTime; }
    
    public Instant getLastSuccessfulResponse() { return lastSuccessfulResponse; }
    public void setLastSuccessfulResponse(Instant lastSuccessfulResponse) { this.lastSuccessfulResponse = lastSuccessfulResponse; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }
    
    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
    
    public int getSuccessfulRequests() { return successfulRequests; }
    public void setSuccessfulRequests(int successfulRequests) { this.successfulRequests = successfulRequests; }
    
    public int getFailedRequests() { return failedRequests; }
    public void setFailedRequests(int failedRequests) { this.failedRequests = failedRequests; }
    
    public int getTimeoutCount() { return timeoutCount; }
    public void setTimeoutCount(int timeoutCount) { this.timeoutCount = timeoutCount; }
    
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    
    public double getCostPerRequest() { return costPerRequest; }
    public void setCostPerRequest(double costPerRequest) { this.costPerRequest = costPerRequest; }
    
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    
    public int getRequestsThisHour() { return requestsThisHour; }
    public void setRequestsThisHour(int requestsThisHour) { this.requestsThisHour = requestsThisHour; }
    
    public int getRequestsThisDay() { return requestsThisDay; }
    public void setRequestsThisDay(int requestsThisDay) { this.requestsThisDay = requestsThisDay; }
    
    public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public boolean isPreferred() { return isPreferred; }
    public void setPreferred(boolean preferred) { isPreferred = preferred; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    // Business methods
    public void recordSuccessfulRequest(long responseTime) {
        this.totalRequests++;
        this.successfulRequests++;
        this.lastSuccessfulResponse = Instant.now();
        this.responseTimeMs = responseTime;
        updateAverageResponseTime(responseTime);
        updateSuccessRate();
        this.status = HealthStatus.HEALTHY;
        this.errorMessage = null;
    }
    
    public void recordFailedRequest(String error) {
        this.totalRequests++;
        this.failedRequests++;
        updateSuccessRate();
        this.status = HealthStatus.UNHEALTHY;
        this.errorMessage = error;
    }
    
    public void recordTimeout() {
        this.totalRequests++;
        this.timeoutCount++;
        updateSuccessRate();
        this.status = HealthStatus.SLOW;
    }
    
    private void updateAverageResponseTime(long newResponseTime) {
        if (successfulRequests == 1) {
            this.averageResponseTimeMs = newResponseTime;
        } else {
            this.averageResponseTimeMs = ((this.averageResponseTimeMs * (successfulRequests - 1)) + newResponseTime) / successfulRequests;
        }
    }
    
    private void updateSuccessRate() {
        if (totalRequests > 0) {
            this.successRate = (double) successfulRequests / totalRequests * 100.0;
        }
    }
    
    public void addCost(double cost) {
        this.totalCost += cost;
    }
    
    public void incrementHourlyRequests() {
        this.requestsThisHour++;
    }
    
    public void incrementDailyRequests() {
        this.requestsThisDay++;
    }
    
    public void resetHourlyRequests() {
        this.requestsThisHour = 0;
    }
    
    public void resetDailyRequests() {
        this.requestsThisDay = 0;
    }
    
    public boolean isHealthy() {
        return status == HealthStatus.HEALTHY && successRate >= 95.0;
    }
    
    public boolean isFast() {
        return averageResponseTimeMs < 1000; // Less than 1 second
    }
    
    public boolean isCostEffective() {
        return costPerRequest < 0.01; // Less than 1 cent per request
    }
    
    public enum HealthStatus {
        HEALTHY,
        UNHEALTHY,
        SLOW,
        UNKNOWN,
        MAINTENANCE
    }
} 