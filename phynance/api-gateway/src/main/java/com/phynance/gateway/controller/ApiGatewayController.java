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

import com.phynance.gateway.model.dto.*;
import com.phynance.gateway.service.AuthService;
import com.phynance.gateway.service.AuditService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

/**
 * REST Controller for API Gateway operations
 */
@RestController
@RequestMapping("/api/gateway")
@CrossOrigin(origins = "*")
public class ApiGatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(ApiGatewayController.class);
    
    private final ApiGatewayService apiGatewayService;
    private final AuthService authService;
    private final AuditService auditService;
    private final RestTemplate restTemplate;
    
    @Autowired
    public ApiGatewayController(ApiGatewayService apiGatewayService, AuthService authService, AuditService auditService) {
        this.apiGatewayService = apiGatewayService;
        this.authService = authService;
        this.auditService = auditService;
        this.restTemplate = new RestTemplate();
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, ip, userAgent);
        setAuthCookies(httpResponse, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.refreshToken(refreshToken, ip, userAgent);
        setAuthCookies(httpResponse, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "accessToken", required = false) String accessToken,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        if (accessToken != null) {
            authService.logout(accessToken, ip, userAgent);
        }
        clearAuthCookies(httpResponse);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var user = (com.phynance.gateway.model.User) authentication.getPrincipal();
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setRole(user.getRole());
        userInfo.setLastLogin(user.getLastLogin());
        userInfo.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var user = (com.phynance.gateway.model.User) authentication.getPrincipal();
        authService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get market data for a symbol - routes to backend-spring
     */
    @GetMapping("/market-data/{symbol}")
    public ResponseEntity<?> getMarketData(@PathVariable String symbol) {
        log.info("Proxying market data request for symbol: {}", symbol);
        
        try {
            String backendUrl = "http://localhost:8081/api/v1/market-data?symbol=" + symbol;
            
            // Forward request to backend-spring
            Object response = restTemplate.getForObject(backendUrl, Object.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error proxying market data request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch market data: " + e.getMessage()));
        }
    }

    /**
     * Get market summary - routes to backend-spring viewer endpoint
     */
    @GetMapping("/viewer/market-summary/{symbol}")
    public ResponseEntity<?> getMarketSummary(@PathVariable String symbol) {
        log.info("Proxying market summary request for symbol: {}", symbol);
        
        try {
            String backendUrl = "http://localhost:8081/api/v1/viewer/market-summary/" + symbol;
            
            // Forward request to backend-spring
            Object response = restTemplate.getForObject(backendUrl, Object.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error proxying market summary request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch market summary: " + e.getMessage()));
        }
    }

    /**
     * Get harmonic oscillator analysis - routes to backend-spring
     */
    @PostMapping("/analysis/harmonic-oscillator")
    public ResponseEntity<?> getHarmonicOscillatorAnalysis(@RequestBody Object analysisRequest) {
        log.info("Proxying harmonic oscillator analysis request");
        
        try {
            String backendUrl = "http://localhost:8081/api/v1/analysis/harmonic-oscillator";
            
            // Forward request to backend-spring
            Object response = restTemplate.postForObject(backendUrl, analysisRequest, Object.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error proxying harmonic oscillator analysis request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to perform harmonic oscillator analysis: " + e.getMessage()));
        }
    }

    /**
     * Get available symbols for viewer
     */
    @GetMapping("/viewer/available-symbols")
    public ResponseEntity<?> getAvailableSymbols() {
        log.info("Proxying available symbols request");
        
        try {
            String backendUrl = "http://localhost:8081/api/v1/viewer/available-symbols";
            
            // Forward request to backend-spring
            Object response = restTemplate.getForObject(backendUrl, Object.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error proxying available symbols request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch available symbols: " + e.getMessage()));
        }
    }

    private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        if (authResponse.getAccessToken() != null) {
            Cookie accessCookie = new Cookie("accessToken", authResponse.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(900); // 15 min
            accessCookie.setSecure(true);
            response.addCookie(accessCookie);
        }
        if (authResponse.getRefreshToken() != null) {
            Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800); // 7 days
            refreshCookie.setSecure(true);
            response.addCookie(refreshCookie);
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        accessCookie.setSecure(true);
        response.addCookie(accessCookie);
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        refreshCookie.setSecure(true);
        response.addCookie(refreshCookie);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
} 