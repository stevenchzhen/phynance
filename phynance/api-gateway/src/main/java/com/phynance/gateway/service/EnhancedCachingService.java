package com.phynance.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced caching service with multi-level caching and intelligent cache management
 */
@Service
public class EnhancedCachingService {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedCachingService.class);
    
    private final SmartRoutingService smartRoutingService;
    private final ApiHealthMonitoringService healthMonitoringService;
    private final Map<String, Object> localCache; // Level 1: In-memory cache
    private final Map<String, Instant> cacheTimestamps;
    private final Map<String, Duration> cacheDurations;
    private final Set<String> popularSymbols;
    private final ScheduledExecutorService scheduler;
    
    @Autowired
    public EnhancedCachingService(SmartRoutingService smartRoutingService,
                                ApiHealthMonitoringService healthMonitoringService) {
        this.smartRoutingService = smartRoutingService;
        this.healthMonitoringService = healthMonitoringService;
        this.localCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
        this.cacheDurations = new ConcurrentHashMap<>();
        this.popularSymbols = initializePopularSymbols();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        initializeCacheWarming();
    }
    
    /**
     * Multi-level cache retrieval
     */
    public Object getCachedData(String symbol, String dataType) {
        String cacheKey = generateCacheKey(symbol, dataType);
        
        // Check local cache
        Object localData = localCache.get(cacheKey);
        if (localData != null && isLocalCacheValid(cacheKey)) {
            log.debug("Cache hit (L1) for key: {}", cacheKey);
            return localData;
        }
        
        log.debug("Cache miss for key: {}", cacheKey);
        return null;
    }
    
    /**
     * Cache data in local cache
     */
    public void cacheData(String symbol, String dataType, Object data) {
        String cacheKey = generateCacheKey(symbol, dataType);
        
        // Determine cache duration based on data type and market hours
        Duration cacheDuration = determineCacheDuration(dataType);
        
        // Local cache
        localCache.put(cacheKey, data);
        cacheTimestamps.put(cacheKey, Instant.now());
        cacheDurations.put(cacheKey, cacheDuration);
        
        log.debug("Cached data for key: {} with duration: {}", cacheKey, cacheDuration);
    }
    
    /**
     * Determine cache duration based on data type and market conditions
     */
    private Duration determineCacheDuration(String dataType) {
        boolean isMarketHours = isMarketHours();
        
        switch (dataType) {
            case "real-time":
                return isMarketHours ? Duration.ofMinutes(1) : Duration.ofMinutes(5);
            case "price":
                return isMarketHours ? Duration.ofMinutes(2) : Duration.ofMinutes(10);
            case "fundamentals":
                return Duration.ofHours(1);
            case "technical":
                return Duration.ofMinutes(15);
            case "news":
                return Duration.ofMinutes(30);
            case "historical":
                return Duration.ofHours(24);
            default:
                return Duration.ofMinutes(5);
        }
    }
    
    /**
     * Check if current time is during market hours
     */
    private boolean isMarketHours() {
        LocalTime now = LocalTime.now(ZoneId.of("America/New_York"));
        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime marketClose = LocalTime.of(16, 0);
        
        return now.isAfter(marketOpen) && now.isBefore(marketClose);
    }
    
    /**
     * Check if local cache entry is still valid
     */
    private boolean isLocalCacheValid(String cacheKey) {
        Instant timestamp = cacheTimestamps.get(cacheKey);
        Duration duration = cacheDurations.get(cacheKey);
        
        if (timestamp == null || duration == null) {
            return false;
        }
        
        return Duration.between(timestamp, Instant.now()).compareTo(duration) < 0;
    }
    
    /**
     * Generate cache key
     */
    private String generateCacheKey(String symbol, String dataType) {
        return String.format("financial_data:%s:%s", symbol.toUpperCase(), dataType);
    }
    
    /**
     * Prefetch popular stocks during off-hours
     */
    @Scheduled(cron = "0 0 6 * * *") // 6 AM daily
    public void prefetchPopularStocks() {
        if (!isMarketHours()) {
            log.info("Starting prefetch of popular stocks");
            
            for (String symbol : popularSymbols) {
                scheduler.schedule(() -> {
                    try {
                        prefetchStockData(symbol);
                    } catch (Exception e) {
                        log.warn("Failed to prefetch data for {}: {}", symbol, e.getMessage());
                    }
                }, new Random().nextInt(300), TimeUnit.SECONDS); // Random delay up to 5 minutes
            }
        }
    }
    
    /**
     * Prefetch data for a specific stock
     */
    private void prefetchStockData(String symbol) {
        String[] dataTypes = {"price", "fundamentals", "technical"};
        
        for (String dataType : dataTypes) {
            try {
                // Use smart routing to get the best provider
                String provider = smartRoutingService.selectBestProvider(symbol, dataType, "US");
                if (provider != null) {
                    // Simulate data fetch (in real implementation, this would call the actual API)
                    Map<String, Object> mockData = createMockData(symbol, dataType);
                    cacheData(symbol, dataType, mockData);
                    log.debug("Prefetched {} data for {}", dataType, symbol);
                }
            } catch (Exception e) {
                log.warn("Failed to prefetch {} data for {}: {}", dataType, symbol, e.getMessage());
            }
        }
    }
    
    /**
     * Cache warming for physics model calculations
     */
    public void warmCacheForPhysicsModels(String symbol) {
        log.info("Warming cache for physics model calculations: {}", symbol);
        
        // Prefetch data needed for physics models
        String[] requiredDataTypes = {"price", "volume", "technical"};
        
        for (String dataType : requiredDataTypes) {
            try {
                Object cachedData = getCachedData(symbol, dataType);
                if (cachedData == null) {
                    // Fetch and cache if not available
                    Map<String, Object> mockData = createMockData(symbol, dataType);
                    cacheData(symbol, dataType, mockData);
                    log.debug("Warmed cache for {}: {}", symbol, dataType);
                }
            } catch (Exception e) {
                log.warn("Failed to warm cache for {} {}: {}", symbol, dataType, e.getMessage());
            }
        }
    }
    
    /**
     * Invalidate cache based on market hours
     */
    @Scheduled(cron = "0 0 9 * * 1-5") // 9 AM on weekdays
    public void invalidateCacheForMarketOpen() {
        log.info("Invalidating cache for market open");
        localCache.clear();
        cacheTimestamps.clear();
        cacheDurations.clear();
    }
    
    /**
     * Clean expired local cache entries
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanExpiredLocalCache() {
        Instant now = Instant.now();
        List<String> expiredKeys = new ArrayList<>();
        
        cacheTimestamps.forEach((key, timestamp) -> {
            Duration duration = cacheDurations.get(key);
            if (duration != null && Duration.between(timestamp, now).compareTo(duration) >= 0) {
                expiredKeys.add(key);
            }
        });
        
        expiredKeys.forEach(key -> {
            localCache.remove(key);
            cacheTimestamps.remove(key);
            cacheDurations.remove(key);
        });
        
        if (!expiredKeys.isEmpty()) {
            log.debug("Cleaned {} expired local cache entries", expiredKeys.size());
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("localCacheSize", localCache.size());
        stats.put("localCacheTimestamps", cacheTimestamps.size());
        stats.put("popularSymbols", popularSymbols.size());
        stats.put("isMarketHours", isMarketHours());
        stats.put("redisCacheSize", 0); // Redis not available
        stats.put("redisError", "Redis not configured");
        
        stats.put("timestamp", Instant.now());
        
        return stats;
    }
    
    /**
     * Initialize popular symbols for prefetching
     */
    private Set<String> initializePopularSymbols() {
        return Set.of("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "NFLX", "SPY", "QQQ");
    }
    
    /**
     * Initialize cache warming
     */
    private void initializeCacheWarming() {
        // Schedule initial cache warming
        scheduler.schedule(() -> {
            log.info("Starting initial cache warming");
            for (String symbol : popularSymbols) {
                warmCacheForPhysicsModels(symbol);
            }
        }, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Create mock data for testing
     */
    private Map<String, Object> createMockData(String symbol, String dataType) {
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("timestamp", Instant.now());
        
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
            case "real-time":
                data.put("price", 150.0 + Math.random() * 50);
                data.put("bid", 149.0 + Math.random() * 50);
                data.put("ask", 151.0 + Math.random() * 50);
                break;
        }
        
        return data;
    }
    
    /**
     * Evict cache for specific symbol and data type
     */
    @CacheEvict(value = "financialData", key = "#symbol + ':' + #dataType")
    public void evictCache(String symbol, String dataType) {
        String cacheKey = generateCacheKey(symbol, dataType);
        localCache.remove(cacheKey);
        cacheTimestamps.remove(cacheKey);
        cacheDurations.remove(cacheKey);
        log.debug("Evicted cache for key: {}", cacheKey);
    }
    
    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        log.info("Clearing all caches");
        localCache.clear();
        cacheTimestamps.clear();
        cacheDurations.clear();
    }
} 