package com.phynance.gateway.service;

import com.google.common.util.concurrent.RateLimiter;
import com.phynance.gateway.config.ApiProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing rate limits across different API providers
 */
@Service
public class RateLimiterService {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final ApiProviderConfig apiProviderConfig;
    
    @Autowired
    public RateLimiterService(ApiProviderConfig apiProviderConfig) {
        this.apiProviderConfig = apiProviderConfig;
        initializeRateLimiters();
    }
    
    /**
     * Initialize rate limiters for all enabled API providers
     */
    private void initializeRateLimiters() {
        apiProviderConfig.getProviders().forEach((providerKey, config) -> {
            if (config.isEnabled()) {
                double ratePerSecond = config.getRateLimit().getRequestsPerSecond();
                RateLimiter rateLimiter = RateLimiter.create(ratePerSecond);
                rateLimiters.put(providerKey, rateLimiter);
                log.info("Initialized rate limiter for {}: {} requests/second", 
                        config.getName(), ratePerSecond);
            }
        });
    }
    
    /**
     * Try to acquire a permit for the specified provider
     * @param provider Provider key
     * @return true if permit acquired, false if rate limit exceeded
     */
    public boolean tryAcquire(String provider) {
        RateLimiter rateLimiter = rateLimiters.get(provider);
        if (rateLimiter == null) {
            log.warn("No rate limiter found for provider: {}", provider);
            return false;
        }
        
        boolean acquired = rateLimiter.tryAcquire();
        if (acquired) {
            log.debug("Rate limit permit acquired for provider: {}", provider);
        } else {
            log.warn("Rate limit exceeded for provider: {}", provider);
        }
        return acquired;
    }
    
    /**
     * Acquire a permit, waiting if necessary
     * @param provider Provider key
     * @return time waited in seconds
     */
    public double acquire(String provider) {
        RateLimiter rateLimiter = rateLimiters.get(provider);
        if (rateLimiter == null) {
            throw new IllegalArgumentException("No rate limiter found for provider: " + provider);
        }
        
        double waitTime = rateLimiter.acquire();
        log.debug("Rate limit permit acquired for provider: {} after {} seconds", provider, waitTime);
        return waitTime;
    }
    
    /**
     * Get the current rate for a provider
     * @param provider Provider key
     * @return rate per second
     */
    public double getRate(String provider) {
        RateLimiter rateLimiter = rateLimiters.get(provider);
        return rateLimiter != null ? rateLimiter.getRate() : 0.0;
    }
    
    /**
     * Check if a provider is available (has rate limiter)
     * @param provider Provider key
     * @return true if provider is available
     */
    public boolean isProviderAvailable(String provider) {
        return rateLimiters.containsKey(provider);
    }
    
    /**
     * Get all available providers
     * @return Set of provider keys
     */
    public java.util.Set<String> getAvailableProviders() {
        return rateLimiters.keySet();
    }
    
    /**
     * Get rate limiter statistics
     * @return Map of provider statistics
     */
    public Map<String, Object> getRateLimiterStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        rateLimiters.forEach((provider, rateLimiter) -> {
            Map<String, Object> providerStats = new ConcurrentHashMap<>();
            providerStats.put("ratePerSecond", rateLimiter.getRate());
            providerStats.put("available", true);
            stats.put(provider, providerStats);
        });
        return stats;
    }
} 