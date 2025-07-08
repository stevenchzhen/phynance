package com.phynance.controller;

import com.phynance.model.HarmonicOscillatorAnalysisRequest;
import com.phynance.model.HarmonicOscillatorAnalysisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.phynance.service.FinancialDataService;
import com.phynance.service.PhysicsModelService;
import com.phynance.model.HarmonicOscillatorRequest;
import com.phynance.model.PhysicsModelResult;
import com.phynance.model.MarketData;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.phynance.service.AuditService;
import com.phynance.service.RateLimitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for harmonic oscillator stock analysis.
 *
 * <p>POST /api/v1/analysis/harmonic-oscillator</p>
 *
 * <p>Request body: { "symbol": "AAPL", ... }</p>
 * <p>Response: { "symbol": "AAPL", ... }</p>
 */
@RestController
@RequestMapping("/api/v1/analysis")
@Validated
public class HarmonicOscillatorAnalysisController {
    @Autowired private FinancialDataService financialDataService;
    @Autowired private PhysicsModelService physicsModelService;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;
    @Value("${limits.trader.max-calculations-per-hour:10}")
    private int traderMaxCalculationsPerHour;

    /**
     * Analyze a stock using the harmonic oscillator model.
     *
     * VIEWER: Only default params, last 30 days, no custom predictionDays
     * TRADER: Limited params, last 2 years, max 10 calculations/hour
     * ANALYST: Full access
     * ADMIN: All access
     */
    @PostMapping("/harmonic-oscillator")
    @PreAuthorize("hasAnyRole('VIEWER','TRADER','ANALYST','ADMIN')")
    @PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
    public ResponseEntity<?> analyze(@RequestBody HarmonicOscillatorAnalysisRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String role = auth != null && auth.getAuthorities().size() > 0 ? auth.getAuthorities().iterator().next().getAuthority() : "UNKNOWN";
        
        // Get client IP for audit logging
        String clientIp = getClientIpAddress();
        
        // Check rate limits
        if (!rateLimitService.checkRateLimit("/api/v1/analysis/harmonic-oscillator")) {
            auditService.logAccessDenied(username, "analyze", "HarmonicOscillatorAnalysisController", "Rate limit exceeded");
            return ResponseEntity.status(429).body("Rate limit exceeded. Please try again later.");
        }
        
        // Check calculation limits
        if (!rateLimitService.checkCalculationLimit("harmonic-oscillator")) {
            auditService.logAccessDenied(username, "analyze", "HarmonicOscillatorAnalysisController", "Calculation limit exceeded");
            return ResponseEntity.status(429).body("Calculation limit exceeded. Please try again later.");
        }
        
        auditService.logSecurityEvent(username, "HARMONIC_OSC_ANALYSIS_ATTEMPT", clientIp, null);

        // Role-based restrictions
        if (role.contains("VIEWER")) {
            // Only allow last 30 days, default params
            if (request.getPredictionDays() != null && request.getPredictionDays() != 5) {
                auditService.logAccessDenied(username, "analyze", "HarmonicOscillatorAnalysisController", "VIEWER: Only default predictionDays allowed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("VIEWER: Only default predictionDays allowed");
            }
            // Enforce last 30 days only
            if (!isWithinLast30Days(request.getStartDate(), request.getEndDate())) {
                auditService.logAccessDenied(username, "analyze", "HarmonicOscillatorAnalysisController", "VIEWER: Only last 30 days allowed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("VIEWER: Only last 30 days of data allowed");
            }
        } else if (role.contains("TRADER")) {
            // Limit to last 2 years
            if (!isWithinLast2Years(request.getStartDate(), request.getEndDate())) {
                auditService.logAccessDenied(username, "analyze", "HarmonicOscillatorAnalysisController", "TRADER: Only last 2 years allowed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("TRADER: Only last 2 years of data allowed");
            }
        } // ANALYST/ADMIN: full access

        // Input validation
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            return ResponseEntity.badRequest().body("Symbol is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start and end dates are required");
        }
        if (request.getPredictionDays() == null || request.getPredictionDays() < 1 || request.getPredictionDays() > 30) {
            return ResponseEntity.badRequest().body("Prediction days must be between 1 and 30");
        }
        // TODO: Validate symbol exists (mocked for now)
        if (!isValidSymbol(request.getSymbol())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid symbol: " + request.getSymbol());
        }
        // Fetch historical OHLCV data
        List<MarketData> marketData = financialDataService.getHistoricalData(request.getSymbol(), request.getStartDate(), request.getEndDate());
        if (marketData == null || marketData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No historical data found for symbol: " + request.getSymbol());
        }
        // Convert to HarmonicOscillatorRequest.Ohlcv
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
        // Build HarmonicOscillatorRequest
        HarmonicOscillatorRequest oscRequest = new HarmonicOscillatorRequest();
        oscRequest.setOhlcvData(ohlcvList);
        oscRequest.setPredictionDays(request.getPredictionDays() != null ? request.getPredictionDays() : 5);
        // Optionally override damping/frequency
        PhysicsModelResult result = physicsModelService.analyze(oscRequest);
        // Backtest: compare oscillator predictions to actual closes in historical window
        HarmonicOscillatorAnalysisResponse resp = new HarmonicOscillatorAnalysisResponse();
        resp.setSymbol(request.getSymbol());
        // Fill analysis entries (backtest)
        List<HarmonicOscillatorAnalysisResponse.AnalysisEntry> analysisEntries = ohlcvList.stream().map((ohlcv) -> {
            HarmonicOscillatorAnalysisResponse.AnalysisEntry entry = new HarmonicOscillatorAnalysisResponse.AnalysisEntry();
            entry.setDate(ohlcv.getDate());
            entry.setActualPrice(ohlcv.getClose());
            // Use oscillator formula for backtest
            double t = ohlcvList.indexOf(ohlcv);
            double oscValue = result.getAmplitude() * Math.exp(-result.getDamping() * t) * Math.cos(result.getFrequency() * t + result.getPhase());
            entry.setOscillatorValue(oscValue + ohlcv.getClose());
            entry.setAmplitude(result.getAmplitude());
            entry.setPhase(result.getPhase());
            // Simple signal: buy if actual > oscillator, sell if <
            if (ohlcv.getClose() > oscValue + ohlcv.getClose()) entry.setSignal("BUY");
            else if (ohlcv.getClose() < oscValue + ohlcv.getClose()) entry.setSignal("SELL");
            else entry.setSignal("HOLD");
            entry.setConfidence(0.8); // Placeholder
            return entry;
        }).collect(Collectors.toList());
        resp.setAnalysis(analysisEntries);
        // Fill predictions
        List<HarmonicOscillatorAnalysisResponse.PredictionEntry> predictionEntries = new java.util.ArrayList<>();
        for (int idx = 0; idx < result.getPredictedPrices().size(); idx++) {
            Double pred = result.getPredictedPrices().get(idx);
            HarmonicOscillatorAnalysisResponse.PredictionEntry p = new HarmonicOscillatorAnalysisResponse.PredictionEntry();
            p.setDate("future-day-" + (idx + 1));
            p.setPredictedPrice(pred);
            p.setSupportLevel(result.getSupportLevels().isEmpty() ? null : result.getSupportLevels().get(0));
            p.setResistanceLevel(result.getResistanceLevels().isEmpty() ? null : result.getResistanceLevels().get(0));
            p.setTrendDirection(result.getSignals().get(idx));
            predictionEntries.add(p);
        }
        resp.setPredictions(predictionEntries);
        // Fill model metrics (mocked for now)
        HarmonicOscillatorAnalysisResponse.ModelMetrics metrics = new HarmonicOscillatorAnalysisResponse.ModelMetrics();
        metrics.setAccuracy(0.75); // Placeholder
        metrics.setCorrelation(0.8); // Placeholder
        metrics.setRmse(2.0); // Placeholder
        resp.setModelMetrics(metrics);
        auditService.logSecurityEvent(username, "HARMONIC_OSC_ANALYSIS_SUCCESS", null, null);
        return ResponseEntity.ok(resp);
    }

    /**
     * Mock symbol validation (replace with real lookup)
     */
    private boolean isValidSymbol(String symbol) {
        return symbol.equalsIgnoreCase("AAPL") || symbol.equalsIgnoreCase("SPY") || symbol.equalsIgnoreCase("TSLA");
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
    
    /**
     * Check if date range is within last 30 days
     */
    private boolean isWithinLast30Days(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate thirtyDaysAgo = java.time.LocalDate.now().minusDays(30);
            
            return start.isAfter(thirtyDaysAgo) && end.isBefore(java.time.LocalDate.now().plusDays(1));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if date range is within last 2 years
     */
    private boolean isWithinLast2Years(String startDate, String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate twoYearsAgo = java.time.LocalDate.now().minusYears(2);
            
            return start.isAfter(twoYearsAgo) && end.isBefore(java.time.LocalDate.now().plusDays(1));
        } catch (Exception e) {
            return false;
        }
    }
} 