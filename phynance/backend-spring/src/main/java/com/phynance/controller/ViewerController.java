package com.phynance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import com.phynance.service.FinancialDataService;
import com.phynance.service.PhysicsModelService;
import com.phynance.model.HarmonicOscillatorRequest;
import com.phynance.model.PhysicsModelResult;
import com.phynance.model.MarketData;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Viewer controller for basic model access with restricted parameters.
 * 
 * VIEWER: Can only view pre-calculated basic models (harmonic oscillator with default parameters)
 */
@RestController
@RequestMapping("/api/v1/viewer")
@PreAuthorize("hasRole('VIEWER')")
public class ViewerController {
    
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;
    @Autowired private FinancialDataService financialDataService;
    @Autowired private PhysicsModelService physicsModelService;
    
    /**
     * Get basic harmonic oscillator analysis with default parameters
     * VIEWER: Only default params, last 30 days, no custom predictionDays
     */
    @GetMapping("/harmonic-oscillator/{symbol}")
    public ResponseEntity<?> getBasicHarmonicOscillator(@PathVariable String symbol) {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        // Check rate limits
        if (!rateLimitService.checkRateLimit("/api/v1/viewer/harmonic-oscillator")) {
            auditService.logAccessDenied(username, "getBasicHarmonicOscillator", "ViewerController", "Rate limit exceeded");
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        
        auditService.logSecurityEvent(username, "VIEWER_HARMONIC_OSCILLATOR_REQUEST", clientIp, null);
        
        // Input validation
        if (symbol == null || symbol.isBlank()) {
            auditService.logError(username, "Invalid request: null or empty symbol", clientIp, null);
            return ResponseEntity.badRequest().body("Symbol is required");
        }
        
        try {
            // Get last 30 days of data (VIEWER restriction)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            List<MarketData> marketData = financialDataService.getHistoricalData(
                symbol, startDate.toString(), endDate.toString());
            
            if (marketData == null || marketData.isEmpty()) {
                auditService.logError(username, "No historical data found for symbol: " + symbol, clientIp, null);
                return ResponseEntity.notFound().build();
            }
            
            // Convert to HarmonicOscillatorRequest.Ohlcv with default parameters
            List<HarmonicOscillatorRequest.Ohlcv> ohlcvList = marketData.stream().map(md -> {
                HarmonicOscillatorRequest.Ohlcv o = new HarmonicOscillatorRequest.Ohlcv();
                o.setDate(md.getTimestamp().toString());
                o.setOpen(md.getOpen());
                o.setHigh(md.getHigh());
                o.setLow(md.getLow());
                o.setClose(md.getClose());
                o.setVolume(md.getVolume());
                return o;
            }).collect(Collectors.toList());
            
            // Build request with default parameters (VIEWER restriction)
            HarmonicOscillatorRequest oscRequest = new HarmonicOscillatorRequest();
            oscRequest.setOhlcvData(ohlcvList);
            oscRequest.setPredictionDays(5); // Default value
            
            PhysicsModelResult result = physicsModelService.analyze(oscRequest);
            
            // Create simplified response for VIEWER
            var response = Map.of(
                "symbol", symbol,
                "analysisType", "Basic Harmonic Oscillator",
                "dataRange", "Last 30 days",
                "predictionDays", 5,
                "currentPrice", marketData.get(marketData.size() - 1).getClose(),
                "predictedPrices", result.getPredictedPrices(),
                "signals", result.getSignals(),
                "supportLevel", result.getSupportLevels().isEmpty() ? null : result.getSupportLevels().get(0),
                "resistanceLevel", result.getResistanceLevels().isEmpty() ? null : result.getResistanceLevels().get(0),
                "message", "Basic analysis with default parameters. Upgrade to TRADER+ for custom parameters."
            );
            
            auditService.logMethodAccess(username, "getBasicHarmonicOscillator", "ViewerController", "SUCCESS");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            auditService.logError(username, "Error in basic harmonic oscillator analysis: " + e.getMessage(), clientIp, null);
            return ResponseEntity.internalServerError().body("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Get basic market data summary (last 30 days only)
     */
    @GetMapping("/market-summary/{symbol}")
    public ResponseEntity<?> getMarketSummary(@PathVariable String symbol) {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        // Check rate limits
        if (!rateLimitService.checkRateLimit("/api/v1/viewer/market-summary")) {
            auditService.logAccessDenied(username, "getMarketSummary", "ViewerController", "Rate limit exceeded");
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        
        auditService.logSecurityEvent(username, "VIEWER_MARKET_SUMMARY_REQUEST", clientIp, null);
        
        try {
            // Get last 30 days of data (VIEWER restriction)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            List<MarketData> marketData = financialDataService.getHistoricalData(
                symbol, startDate.toString(), endDate.toString());
            
            if (marketData == null || marketData.isEmpty()) {
                auditService.logError(username, "No historical data found for symbol: " + symbol, clientIp, null);
                return ResponseEntity.notFound().build();
            }
            
            // Calculate basic summary statistics
            double currentPrice = marketData.get(marketData.size() - 1).getClose();
            double startPrice = marketData.get(0).getClose();
            double change = currentPrice - startPrice;
            double changePercent = (change / startPrice) * 100;
            
            double high = marketData.stream().mapToDouble(MarketData::getHigh).max().orElse(0);
            double low = marketData.stream().mapToDouble(MarketData::getLow).min().orElse(0);
            
            var summary = Map.of(
                "symbol", symbol,
                "currentPrice", currentPrice,
                "change", change,
                "changePercent", changePercent,
                "periodHigh", high,
                "periodLow", low,
                "dataPoints", marketData.size(),
                "dataRange", "Last 30 days",
                "message", "Basic market summary. Upgrade to TRADER+ for extended historical data."
            );
            
            auditService.logMethodAccess(username, "getMarketSummary", "ViewerController", "SUCCESS");
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            auditService.logError(username, "Error in market summary: " + e.getMessage(), clientIp, null);
            return ResponseEntity.internalServerError().body("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Get available symbols for VIEWER access
     */
    @GetMapping("/available-symbols")
    public ResponseEntity<?> getAvailableSymbols() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "VIEWER_AVAILABLE_SYMBOLS_REQUEST", clientIp, null);
        
        // Limited symbols for VIEWER role
        var symbols = List.of("AAPL", "SPY", "TSLA", "MSFT", "GOOGL");
        
        var response = Map.of(
            "symbols", symbols,
            "count", symbols.size(),
            "message", "Basic symbol list. Upgrade to TRADER+ for extended symbol access."
        );
        
        auditService.logMethodAccess(username, "getAvailableSymbols", "ViewerController", "SUCCESS");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's current usage statistics
     */
    @GetMapping("/usage-stats")
    public ResponseEntity<?> getUsageStats() {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress();
        
        auditService.logSecurityEvent(username, "VIEWER_USAGE_STATS_REQUEST", clientIp, null);
        
        var stats = Map.of(
            "role", "VIEWER",
            "apiRequestsThisHour", 3, // Mock value
            "apiRequestsLimit", 10,
            "calculationsThisHour", 2, // Mock value
            "calculationsLimit", 5,
            "dataAccessLimit", "30 days",
            "symbolLimit", 1,
            "message", "Basic usage statistics. Upgrade to TRADER+ for extended limits."
        );
        
        auditService.logMethodAccess(username, "getUsageStats", "ViewerController", "SUCCESS");
        return ResponseEntity.ok(stats);
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
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