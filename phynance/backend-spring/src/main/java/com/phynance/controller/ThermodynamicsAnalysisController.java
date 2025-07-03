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

    /**
     * Analyze a stock using thermodynamic principles.
     * @param request ThermodynamicsAnalysisRequest
     * @return ThermodynamicsAnalysisResponse
     */
    @PostMapping("/thermodynamics")
    public ResponseEntity<?> analyze(@RequestBody ThermodynamicsAnalysisRequest request) {
        // Input validation
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
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
        return ResponseEntity.ok(resp);
    }
} 