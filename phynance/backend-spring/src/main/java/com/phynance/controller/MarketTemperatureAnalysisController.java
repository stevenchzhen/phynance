package com.phynance.controller;

import com.phynance.model.MarketTemperatureAnalysisRequest;
import com.phynance.model.MarketTemperatureAnalysisResponse;
import com.phynance.model.MarketData;
import com.phynance.service.FinancialDataService;
import com.phynance.service.MarketTemperatureAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for market temperature analysis.
 *
 * <p>POST /api/v1/analysis/market-temperature</p>
 *
 * <p>Request body: { "symbols": ["AAPL", ...], ... }</p>
 * <p>Response: { "marketTemperature": { ... }, ... }</p>
 */
@RestController
@RequestMapping("/api/v1/analysis")
public class MarketTemperatureAnalysisController {
    @Autowired private FinancialDataService financialDataService;
    @Autowired private MarketTemperatureAnalysisService marketTemperatureAnalysisService;

    /**
     * Analyze market temperature for a set of symbols.
     * @param request MarketTemperatureAnalysisRequest
     * @return MarketTemperatureAnalysisResponse
     */
    @PostMapping("/market-temperature")
    public ResponseEntity<?> analyze(@RequestBody MarketTemperatureAnalysisRequest request) {
        // Input validation
        if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
            return ResponseEntity.badRequest().body("At least one symbol is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start and end dates are required");
        }
        if (request.getTemperatureWindow() != null && (request.getTemperatureWindow() < 2 || request.getTemperatureWindow() > 365)) {
            return ResponseEntity.badRequest().body("Temperature window must be between 2 and 365");
        }
        // Fetch OHLCV data for all symbols
        List<List<MarketData>> allOhlcv = new ArrayList<>();
        for (String symbol : request.getSymbols()) {
            List<MarketData> ohlcv = financialDataService.getHistoricalData(symbol, request.getStartDate(), request.getEndDate());
            if (ohlcv == null || ohlcv.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No historical data found for symbol: " + symbol);
            }
            allOhlcv.add(ohlcv);
        }
        try {
            MarketTemperatureAnalysisResponse resp = marketTemperatureAnalysisService.analyze(request, allOhlcv);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during analysis: " + e.getMessage());
        }
    }
} 