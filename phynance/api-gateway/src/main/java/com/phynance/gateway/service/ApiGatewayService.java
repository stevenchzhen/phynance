package com.phynance.gateway.service;

import com.phynance.gateway.config.ApiProviderConfig;
import com.phynance.gateway.model.ApiRequest;
import com.phynance.gateway.model.RequestPriority;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Main API Gateway service that orchestrates rate limiting, queuing, and request distribution
 */
@Service
public class ApiGatewayService {
    
    private static final Logger log = LoggerFactory.getLogger(ApiGatewayService.class);
    
    private final RateLimiterService rateLimiterService;
    private final RequestQueueService queueService;
    private final ApiProviderConfig apiProviderConfig;
    private final WebClient webClient;
    private final Map<String, CircuitBreaker> circuitBreakers;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired
    public ApiGatewayService(RateLimiterService rateLimiterService, 
                           RequestQueueService queueService,
                           ApiProviderConfig apiProviderConfig) {
        this.rateLimiterService = rateLimiterService;
        this.queueService = queueService;
        this.apiProviderConfig = apiProviderConfig;
        this.webClient = WebClient.builder().build();
        this.circuitBreakers = new ConcurrentHashMap<>();
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        initializeCircuitBreakers();
    }
    
    /**
     * Initialize circuit breakers for all API providers
     */
    private void initializeCircuitBreakers() {
        apiProviderConfig.getProviders().forEach((providerKey, config) -> {
            if (config.isEnabled()) {
                CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                        .failureRateThreshold(config.getCircuitBreaker().getFailureRateThreshold())
                        .waitDurationInOpenState(Duration.parse("PT" + config.getCircuitBreaker().getWaitDurationInOpenState()))
                        .slidingWindowSize(config.getCircuitBreaker().getSlidingWindowSize())
                        .minimumNumberOfCalls(config.getCircuitBreaker().getMinimumNumberOfCalls())
                        .build();
                
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(providerKey, circuitBreakerConfig);
                circuitBreakers.put(providerKey, circuitBreaker);
                log.info("Initialized circuit breaker for provider: {}", providerKey);
            }
        });
    }
    
    /**
     * Submit a request with priority and get a CompletableFuture
     */
    public CompletableFuture<Object> submitRequest(String symbol, String endpoint, RequestPriority priority) {
        ApiRequest request = ApiRequest.create(symbol, endpoint, priority);
        
        // Add to queue
        boolean queued = queueService.enqueue(request);
        if (!queued) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Queue is full"));
            return future;
        }
        
        // Process asynchronously
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processRequest(request);
            } catch (Exception e) {
                log.error("Error processing request: {}", request.getId(), e);
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Process a request with intelligent provider selection
     */
    private Object processRequest(ApiRequest request) {
        List<String> availableProviders = getAvailableProviders();
        
        for (String provider : availableProviders) {
            try {
                if (tryProcessWithProvider(request, provider)) {
                    return request.getResponseData();
                }
            } catch (Exception e) {
                log.warn("Failed to process request {} with provider {}: {}", 
                        request.getId(), provider, e.getMessage());
                continue;
            }
        }
        
        // All providers failed, try cached data
        return getCachedData(request);
    }
    
    /**
     * Try to process a request with a specific provider
     */
    private boolean tryProcessWithProvider(ApiRequest request, String provider) {
        CircuitBreaker circuitBreaker = circuitBreakers.get(provider);
        if (circuitBreaker == null) {
            return false;
        }
        
        return circuitBreaker.executeSupplier(() -> {
            // Check rate limit
            if (!rateLimiterService.tryAcquire(provider)) {
                throw new RuntimeException("Rate limit exceeded for provider: " + provider);
            }
            
            // Make API call
            String url = buildApiUrl(provider, request);
            Object response = makeApiCall(url, provider);
            
            // Cache the response
            cacheResponse(request, response);
            
            request.markCompleted(response);
            return true;
        });
    }
    
    /**
     * Make an API call to the specified provider
     */
    private Object makeApiCall(String url, String provider) {
        ApiProviderConfig.ProviderConfig config = apiProviderConfig.getProviders().get(provider);
        Duration timeout = Duration.parse("PT" + config.getTimeout());
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(timeout)
                .block();
    }
    
    /**
     * Build API URL for the provider
     */
    private String buildApiUrl(String provider, ApiRequest request) {
        ApiProviderConfig.ProviderConfig config = apiProviderConfig.getProviders().get(provider);
        String baseUrl = config.getBaseUrl();
        
        switch (provider) {
            case "yahoo-finance":
                return String.format("%s/v8/finance/chart/%s?interval=1d&range=1d", baseUrl, request.getSymbol());
            case "alpha-vantage":
                return String.format("%s/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=DEMO", baseUrl, request.getSymbol());
            case "twelve-data":
                return String.format("%s/time_series?symbol=%s&interval=1day&apikey=DEMO", baseUrl, request.getSymbol());
            case "polygon":
                return String.format("%s/v2/aggs/ticker/%s/range/1/day/2023-01-09/2023-01-09?apikey=DEMO", baseUrl, request.getSymbol());
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
    
    /**
     * Get available providers in priority order
     */
    private List<String> getAvailableProviders() {
        return apiProviderConfig.getProviders().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .sorted(Map.Entry.comparingByValue((a, b) -> Integer.compare(a.getPriority(), b.getPriority())))
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Cache response data
     */
    @Cacheable(value = "apiResponses", key = "#request.symbol + '-' + #request.endpoint")
    public Object cacheResponse(ApiRequest request, Object response) {
        log.debug("Cached response for request: {}", request.getId());
        return response;
    }
    
    /**
     * Get cached data for a request
     */
    @Cacheable(value = "apiResponses", key = "#request.symbol + '-' + #request.endpoint")
    public Object getCachedData(ApiRequest request) {
        log.warn("No cached data available for request: {}", request.getId());
        return null;
    }
    
    /**
     * Get gateway statistics
     */
    public Map<String, Object> getGatewayStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("rateLimiterStats", rateLimiterService.getRateLimiterStats());
        stats.put("queueStats", queueService.getQueueStats());
        stats.put("circuitBreakerStats", getCircuitBreakerStats());
        return stats;
    }
    
    /**
     * Get circuit breaker statistics
     */
    private Map<String, Object> getCircuitBreakerStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        circuitBreakers.forEach((provider, circuitBreaker) -> {
            Map<String, Object> providerStats = new ConcurrentHashMap<>();
            providerStats.put("state", circuitBreaker.getState());
            providerStats.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
            providerStats.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
            providerStats.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
            stats.put(provider, providerStats);
        });
        return stats;
    }
    
    /**
     * Cancel a request
     */
    public boolean cancelRequest(String requestId) {
        return queueService.cancelRequest(requestId);
    }
    
    /**
     * Get estimated wait time for a priority level
     */
    public long getEstimatedWaitTime(RequestPriority priority) {
        return queueService.getEstimatedWaitTime(priority);
    }
} 