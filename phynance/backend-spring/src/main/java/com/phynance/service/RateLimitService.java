package com.phynance.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    
    private final Map<String, UserRateLimit> userRateLimits = new ConcurrentHashMap<>();
    private final AuditService auditService;
    
    // Role-based rate limits
    private static final int VIEWER_RATE_LIMIT = 10; // 10 requests per hour
    private static final int TRADER_RATE_LIMIT = 100; // 100 requests per hour
    private static final int ANALYST_RATE_LIMIT = 1000; // 1000 requests per hour
    private static final int ADMIN_RATE_LIMIT = Integer.MAX_VALUE; // Unlimited
    
    // Role-based calculation limits
    private static final int VIEWER_CALC_LIMIT = 5; // 5 calculations per hour
    private static final int TRADER_CALC_LIMIT = 10; // 10 calculations per hour
    private static final int ANALYST_CALC_LIMIT = Integer.MAX_VALUE; // Unlimited
    private static final int ADMIN_CALC_LIMIT = Integer.MAX_VALUE; // Unlimited
    
    public RateLimitService(AuditService auditService) {
        this.auditService = auditService;
    }
    
    public boolean checkRateLimit(String endpoint) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();
        
        UserRateLimit rateLimit = userRateLimits.computeIfAbsent(username, k -> new UserRateLimit());
        
        int limit = getRateLimitForRole(role);
        boolean allowed = rateLimit.checkApiLimit(limit);
        
        if (!allowed) {
            auditService.logRateLimitExceeded(username, endpoint, role + " limit: " + limit + "/hour");
        }
        
        return allowed;
    }
    
    public boolean checkCalculationLimit(String modelType) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();
        
        UserRateLimit rateLimit = userRateLimits.computeIfAbsent(username, k -> new UserRateLimit());
        
        int limit = getCalculationLimitForRole(role);
        boolean allowed = rateLimit.checkCalculationLimit(limit);
        
        if (!allowed) {
            auditService.logRateLimitExceeded(username, "calculation:" + modelType, role + " calc limit: " + limit + "/hour");
        }
        
        return allowed;
    }
    
    public boolean checkDataAccessLimit(String symbol, int dataPoints) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();
        
        // Data access limits by role
        int maxDataPoints = getDataAccessLimitForRole(role);
        boolean allowed = dataPoints <= maxDataPoints;
        
        if (allowed) {
            auditService.logDataAccess(username, "historical", symbol, dataPoints);
        } else {
            auditService.logAccessDenied(username, "getHistoricalData", "FinancialDataService", 
                "Data points " + dataPoints + " exceeds limit " + maxDataPoints + " for role " + role);
        }
        
        return allowed;
    }
    
    public boolean checkSymbolLimit(int symbolCount) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();
        
        int maxSymbols = getSymbolLimitForRole(role);
        boolean allowed = symbolCount <= maxSymbols;
        
        if (!allowed) {
            auditService.logAccessDenied(username, "getMultipleSymbols", "FinancialDataService", 
                "Symbol count " + symbolCount + " exceeds limit " + maxSymbols + " for role " + role);
        }
        
        return allowed;
    }
    
    private int getRateLimitForRole(String role) {
        return switch (role) {
            case "ROLE_VIEWER" -> VIEWER_RATE_LIMIT;
            case "ROLE_TRADER" -> TRADER_RATE_LIMIT;
            case "ROLE_ANALYST" -> ANALYST_RATE_LIMIT;
            case "ROLE_ADMIN" -> ADMIN_RATE_LIMIT;
            default -> VIEWER_RATE_LIMIT;
        };
    }
    
    private int getCalculationLimitForRole(String role) {
        return switch (role) {
            case "ROLE_VIEWER" -> VIEWER_CALC_LIMIT;
            case "ROLE_TRADER" -> TRADER_CALC_LIMIT;
            case "ROLE_ANALYST" -> ANALYST_CALC_LIMIT;
            case "ROLE_ADMIN" -> ADMIN_CALC_LIMIT;
            default -> VIEWER_CALC_LIMIT;
        };
    }
    
    private int getDataAccessLimitForRole(String role) {
        return switch (role) {
            case "ROLE_VIEWER" -> 30; // 30 days
            case "ROLE_TRADER" -> 730; // 2 years
            case "ROLE_ANALYST", "ROLE_ADMIN" -> Integer.MAX_VALUE; // Unlimited
            default -> 30;
        };
    }
    
    private int getSymbolLimitForRole(String role) {
        return switch (role) {
            case "ROLE_VIEWER" -> 1; // 1 symbol at a time
            case "ROLE_TRADER" -> 20; // 20 symbols simultaneously
            case "ROLE_ANALYST", "ROLE_ADMIN" -> Integer.MAX_VALUE; // Unlimited
            default -> 1;
        };
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_VIEWER");
        }
        return "ROLE_VIEWER";
    }
    
    private static class UserRateLimit {
        private final AtomicInteger apiCount = new AtomicInteger(0);
        private final AtomicInteger calculationCount = new AtomicInteger(0);
        private LocalDateTime lastReset = LocalDateTime.now();
        
        public boolean checkApiLimit(int limit) {
            resetIfNeeded();
            return apiCount.incrementAndGet() <= limit;
        }
        
        public boolean checkCalculationLimit(int limit) {
            resetIfNeeded();
            return calculationCount.incrementAndGet() <= limit;
        }
        
        private void resetIfNeeded() {
            LocalDateTime now = LocalDateTime.now();
            if (now.getHour() != lastReset.getHour() || now.getDayOfYear() != lastReset.getDayOfYear()) {
                apiCount.set(0);
                calculationCount.set(0);
                lastReset = now;
            }
        }
    }
} 