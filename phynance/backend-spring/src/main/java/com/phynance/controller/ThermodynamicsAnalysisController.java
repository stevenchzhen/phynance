package com.phynance.controller;

import com.phynance.model.ThermodynamicsAnalysisRequest;
import com.phynance.model.ThermodynamicsAnalysisResponse;
import com.phynance.model.MarketData;
import com.phynance.service.FinancialDataService;
import com.phynance.service.ThermodynamicsAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for thermodynamic stock market analysis.
 *
 * <p>POST /api/v1/analysis/thermodynamics</p>
 *
 * <p>Request body: { "symbol": "AAPL", ... }</p>
 * <p>Response: { "symbol": "AAPL", ... }</p>
 */
@RestController
@RequestMapping("/api/v1/analysis")
public class ThermodynamicsAnalysisController {
    @Autowired private FinancialDataService financialDataService;
    @Autowired private ThermodynamicsAnalysisService thermodynamicsAnalysisService;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;

    /**
     * Analyze a stock using thermodynamic principles.
     * 
     * ANALYST+: Advanced physics models require ANALYST+ role
     * @param request ThermodynamicsAnalysisRequest
     * @return ThermodynamicsAnalysisResponse
     */
    @PostMapping("/thermodynamics")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
    public ResponseEntity<?> analyze(@RequestBody ThermodynamicsAnalysisRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Get client IP for audit logging
        String clientIp = getClientIpAddress();
        
        // Check rate limits
        if (!rateLimitService.checkRateLimit("/api/v1/analysis/thermodynamics")) {
            auditService.logAccessDenied(username, "analyze", "ThermodynamicsAnalysisController", "Rate limit exceeded");
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        
        // Check calculation limits
        if (!rateLimitService.checkCalculationLimit("thermodynamics")) {
            auditService.logAccessDenied(username, "analyze", "ThermodynamicsAnalysisController", "Calculation limit exceeded");
            return ResponseEntity.status(429).body("Calculation limit exceeded. Please try again later.");
        }
        
        auditService.logSecurityEvent(username, "THERMODYNAMICS_ANALYSIS_ATTEMPT", clientIp, null);
        
        // Input validation
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            auditService.logError(username, "Invalid request: null or empty symbol", clientIp, null);
            return ResponseEntity.badRequest().body("Symbol is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start and end dates are required");
        }
        // Fetch OHLCV data for main symbol
        List<MarketData> mainOhlcv = financialDataService.getHistoricalData(request.getSymbol(), request.getStartDate(), request.getEndDate());
        if (mainOhlcv == null || mainOhlcv.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No historical data found for symbol: " + request.getSymbol());
        }
        // Fetch OHLCV data for related symbols (sector correlation)
        List<List<MarketData>> relatedOhlcv = new ArrayList<>();
        if (request.getRelatedSymbols() != null) {
            for (String rel : request.getRelatedSymbols()) {
                List<MarketData> relData = financialDataService.getHistoricalData(rel, request.getStartDate(), request.getEndDate());
                if (relData != null && !relData.isEmpty()) {
                    relatedOhlcv.add(relData);
                }
            }
        }
        // Run analysis
        ThermodynamicsAnalysisResponse resp = thermodynamicsAnalysisService.analyze(request, mainOhlcv, relatedOhlcv);
        auditService.logSecurityEvent(username, "THERMODYNAMICS_ANALYSIS_SUCCESS", clientIp, null);
        return ResponseEntity.ok(resp);
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