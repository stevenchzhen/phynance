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
import com.phynance.model.MarketDataDto;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Analyze a stock using the harmonic oscillator model.
     * @param request HarmonicOscillatorAnalysisRequest
     * @return HarmonicOscillatorAnalysisResponse
     */
    @PostMapping("/harmonic-oscillator")
    public ResponseEntity<?> analyze(@RequestBody HarmonicOscillatorAnalysisRequest request) {
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
        List<MarketDataDto> marketData = financialDataService.getHistoricalData(request.getSymbol(), request.getStartDate(), request.getEndDate());
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
        List<HarmonicOscillatorAnalysisResponse.PredictionEntry> predictionEntries = result.getPredictedPrices().stream().map((pred, idx) -> {
            HarmonicOscillatorAnalysisResponse.PredictionEntry p = new HarmonicOscillatorAnalysisResponse.PredictionEntry();
            p.setDate("future-day-" + (idx + 1));
            p.setPredictedPrice(pred);
            p.setSupportLevel(result.getSupportLevels().isEmpty() ? null : result.getSupportLevels().get(0));
            p.setResistanceLevel(result.getResistanceLevels().isEmpty() ? null : result.getResistanceLevels().get(0));
            p.setTrendDirection(result.getSignals().get(idx));
            return p;
        }).collect(Collectors.toList());
        resp.setPredictions(predictionEntries);
        // Fill model metrics (mocked for now)
        HarmonicOscillatorAnalysisResponse.ModelMetrics metrics = new HarmonicOscillatorAnalysisResponse.ModelMetrics();
        metrics.setAccuracy(0.75); // Placeholder
        metrics.setCorrelation(0.8); // Placeholder
        metrics.setRmse(2.0); // Placeholder
        resp.setModelMetrics(metrics);
        return ResponseEntity.ok(resp);
    }

    /**
     * Mock symbol validation (replace with real lookup)
     */
    private boolean isValidSymbol(String symbol) {
        return symbol.equalsIgnoreCase("AAPL") || symbol.equalsIgnoreCase("SPY") || symbol.equalsIgnoreCase("TSLA");
    }
} 