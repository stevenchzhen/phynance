package com.phynance.controller;

import com.phynance.model.MarketData;
import com.phynance.service.FinancialDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@RestController
public class MarketDataController {
    private final FinancialDataService financialDataService;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;

    @Autowired
    public MarketDataController(FinancialDataService financialDataService) {
        this.financialDataService = financialDataService;
    }

    /**
     * Get real-time market data for a symbol.
     * 
     * TRADER+: Real-time data streaming requires TRADER+ role
     */
    @GetMapping("/api/v1/market-data")
    @PreAuthorize("hasAnyRole('TRADER','ANALYST','ADMIN')")
    @PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
    public ResponseEntity<?> getMarketData(@RequestParam String symbol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Get client IP for audit logging
        String clientIp = getClientIpAddress();
        
        // Check rate limits
        if (!rateLimitService.checkRateLimit("/api/v1/market-data")) {
            auditService.logAccessDenied(username, "getMarketData", "MarketDataController", "Rate limit exceeded");
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        
        auditService.logSecurityEvent(username, "MARKET_DATA_REQUEST", clientIp, null);
        
        // Input validation
        if (symbol == null || symbol.isBlank()) {
            auditService.logError(username, "Invalid request: null or empty symbol", clientIp, null);
            return ResponseEntity.badRequest().body("Symbol is required");
        }
        
        MarketData marketData = financialDataService.getMarketData(symbol);
        if (marketData == null) {
            auditService.logError(username, "No market data found for symbol: " + symbol, clientIp, null);
            return ResponseEntity.notFound().build();
        }
        
        auditService.logDataAccess(username, "realtime", symbol, 1);
        return ResponseEntity.ok(marketData);
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        return "unknown";
    }
} 