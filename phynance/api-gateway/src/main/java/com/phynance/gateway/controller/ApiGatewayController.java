package com.phynance.gateway.controller;

import com.phynance.gateway.model.RequestPriority;
import com.phynance.gateway.service.ApiGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for API Gateway operations
 */
@RestController
@RequestMapping("/api/gateway")
@CrossOrigin(origins = "*")
public class ApiGatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(ApiGatewayController.class);
    
    private final ApiGatewayService apiGatewayService;
    
    @Autowired
    public ApiGatewayController(ApiGatewayService apiGatewayService) {
        this.apiGatewayService = apiGatewayService;
    }
    
    /**
     * Submit a high priority request (physics model calculations)
     */
    @PostMapping("/request/high")
    public CompletableFuture<ResponseEntity<Object>> submitHighPriorityRequest(
            @RequestParam String symbol,
            @RequestParam String endpoint) {
        
        log.info("Submitting high priority request for symbol: {}, endpoint: {}", symbol, endpoint);
        
        return apiGatewayService.submitRequest(symbol, endpoint, RequestPriority.HIGH)
                .thenApply(response -> {
                    if (response != null) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.noContent().build();
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error processing high priority request", throwable);
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", throwable.getMessage()));
                });
    }
    
    /**
     * Submit a medium priority request (user dashboard requests)
     */
    @PostMapping("/request/medium")
    public CompletableFuture<ResponseEntity<Object>> submitMediumPriorityRequest(
            @RequestParam String symbol,
            @RequestParam String endpoint) {
        
        log.info("Submitting medium priority request for symbol: {}, endpoint: {}", symbol, endpoint);
        
        return apiGatewayService.submitRequest(symbol, endpoint, RequestPriority.MEDIUM)
                .thenApply(response -> {
                    if (response != null) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.noContent().build();
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error processing medium priority request", throwable);
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", throwable.getMessage()));
                });
    }
    
    /**
     * Submit a low priority request (background data updates)
     */
    @PostMapping("/request/low")
    public CompletableFuture<ResponseEntity<Object>> submitLowPriorityRequest(
            @RequestParam String symbol,
            @RequestParam String endpoint) {
        
        log.info("Submitting low priority request for symbol: {}, endpoint: {}", symbol, endpoint);
        
        return apiGatewayService.submitRequest(symbol, endpoint, RequestPriority.LOW)
                .thenApply(response -> {
                    if (response != null) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.noContent().build();
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error processing low priority request", throwable);
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", throwable.getMessage()));
                });
    }
    
    /**
     * Get gateway statistics and monitoring data
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGatewayStats() {
        log.debug("Retrieving gateway statistics");
        return ResponseEntity.ok(apiGatewayService.getGatewayStats());
    }
    
    /**
     * Get estimated wait time for a priority level
     */
    @GetMapping("/wait-time/{priority}")
    public ResponseEntity<Map<String, Object>> getEstimatedWaitTime(@PathVariable String priority) {
        try {
            RequestPriority requestPriority = RequestPriority.valueOf(priority.toUpperCase());
            long waitTime = apiGatewayService.getEstimatedWaitTime(requestPriority);
            
            return ResponseEntity.ok(Map.of(
                    "priority", priority,
                    "estimatedWaitTimeMs", waitTime,
                    "estimatedWaitTimeSeconds", waitTime / 1000.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid priority: " + priority));
        }
    }
    
    /**
     * Cancel a request
     */
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<Map<String, Object>> cancelRequest(@PathVariable String requestId) {
        log.info("Cancelling request: {}", requestId);
        
        boolean cancelled = apiGatewayService.cancelRequest(requestId);
        
        if (cancelled) {
            return ResponseEntity.ok(Map.of(
                    "message", "Request cancelled successfully",
                    "requestId", requestId
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "API Gateway",
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Get available API providers
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        Map<String, Object> stats = apiGatewayService.getGatewayStats();
        return ResponseEntity.ok(Map.of(
                "providers", stats.get("rateLimiterStats"),
                "timestamp", System.currentTimeMillis()
        ));
    }
} 