package com.phynance.service;

import com.phynance.model.WavePhysicsAnalysisRequest;
import com.phynance.model.WavePhysicsAnalysisResponse;
import com.phynance.model.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WavePhysicsService {
    @Autowired
    private FinancialDataService financialDataService;

    public WavePhysicsAnalysisResponse analyze(WavePhysicsAnalysisRequest request) {
        List<MarketData> data = financialDataService.getHistoricalData(
                request.getSymbol(), request.getStartDate(), request.getEndDate());
        return analyze(request, data);
    }

    @Cacheable(value = "wavePhysics", key = "#request.toString().concat('-').concat(#data.hashCode().toString())")
    public WavePhysicsAnalysisResponse analyze(WavePhysicsAnalysisRequest request, List<MarketData> data) {
        if (data == null || data.size() < 10) {
            throw new RuntimeException("Not enough historical data for wave analysis (minimum 10 data points required)");
        }
        // Extract closing prices
        List<Double> closes = data.stream().map(MarketData::getClose).collect(Collectors.toList());
        int n = closes.size();
        double[] t = new double[n];
        for (int i = 0; i < n; i++) t[i] = i;

        // Heuristic: estimate frequencies for short (daily), medium (weekly), long (monthly)
        double w1 = 2 * Math.PI / 24;  // ~1 day (24 hours)
        double w2 = 2 * Math.PI / 168; // ~1 week (168 hours)
        double w3 = 2 * Math.PI / 720; // ~1 month (720 hours)

        // Estimate amplitudes and phases (simple DFT-like projection)
        double[] A = new double[3];
        double[] phi = new double[3];
        double[] w = new double[]{w1, w2, w3};
        
        // Calculate mean price for normalization
        double meanPrice = closes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        for (int j = 0; j < 3; j++) {
            double sumCos = 0, sumSin = 0;
            for (int i = 0; i < n; i++) {
                // Normalize by mean price to get relative amplitudes
                double normalizedPrice = (closes.get(i) - meanPrice) / meanPrice;
                sumCos += normalizedPrice * Math.cos(w[j] * t[i]);
                sumSin += normalizedPrice * Math.sin(w[j] * t[i]);
            }
            A[j] = 2 * Math.sqrt(sumCos * sumCos + sumSin * sumSin) / n;
            phi[j] = Math.atan2(-sumSin, sumCos); // negative for finance convention
        }

        // Calculate total wave at each t
        List<Double> totalWave = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double val = 0;
            for (int j = 0; j < 3; j++) {
                val += A[j] * Math.cos(w[j] * t[i] + phi[j]);
            }
            totalWave.add(val);
        }

        // Interference: find peaks (constructive) and troughs (destructive)
        List<Double> supportLevels = new ArrayList<>();
        List<Double> resistanceLevels = new ArrayList<>();
        List<Double> nodeLevels = new ArrayList<>();
        for (int i = 1; i < n - 1; i++) {
            if (totalWave.get(i) > totalWave.get(i - 1) && totalWave.get(i) > totalWave.get(i + 1)) {
                resistanceLevels.add(closes.get(i));
            }
            if (totalWave.get(i) < totalWave.get(i - 1) && totalWave.get(i) < totalWave.get(i + 1)) {
                supportLevels.add(closes.get(i));
            }
            if (Math.abs(totalWave.get(i)) < 0.01 * Arrays.stream(A).max().orElse(1.0)) {
                nodeLevels.add(closes.get(i));
            }
        }

        // Trading signals based on wave patterns and price trends
        List<String> tradingSignals = new ArrayList<>();
        double priceChange = closes.get(n-1) - closes.get(0); // Overall price trend
        double avgWave = totalWave.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maxAmplitude = Arrays.stream(A).max().orElse(0.1);
        
        for (int i = 1; i < n; i++) {
            double prev = totalWave.get(i - 1);
            double curr = totalWave.get(i);
            double priceNow = closes.get(i);
            double pricePrev = closes.get(i - 1);
            double waveDelta = curr - prev;
            
            // Consider both wave interference and actual price movement
            if (curr > 0.3 * maxAmplitude && waveDelta > 0.1 * maxAmplitude) {
                tradingSignals.add("BUY");
            } else if (curr < -0.3 * maxAmplitude && waveDelta < -0.1 * maxAmplitude) {
                tradingSignals.add("SELL");  
            } else if (Math.abs(waveDelta) < 0.05 * maxAmplitude) {
                tradingSignals.add("HOLD");
            } else if (priceNow > pricePrev && curr > prev) {
                tradingSignals.add("BUY");
            } else if (priceNow < pricePrev && curr < prev) {
                tradingSignals.add("SELL");
            } else {
                tradingSignals.add("HOLD");
            }
        }

        // Predict future levels (1-4 weeks) - make predictions relative to current price
        List<Double> predictedLevels = new ArrayList<>();
        double currentPrice = closes.get(n - 1);
        
        for (int k = 1; k <= request.getPredictionWeeks() * 5; k++) {
            double tFuture = n + k;
            double val = 0;
            for (int j = 0; j < 3; j++) {
                val += A[j] * Math.cos(w[j] * tFuture + phi[j]);
            }
            // Scale the prediction to be more conservative (max 5% change from current price)
            double scaledChange = val * 0.05 * currentPrice / maxAmplitude;
            double predictedPrice = currentPrice + scaledChange;
            
            // Ensure predictions stay within reasonable bounds (±10% of current price)
            double minPrice = currentPrice * 0.9;
            double maxPrice = currentPrice * 1.1;
            predictedPrice = Math.max(minPrice, Math.min(maxPrice, predictedPrice));
            
            predictedLevels.add(predictedPrice);
        }

        // Explanation
        StringBuilder explanation = new StringBuilder();
        explanation.append("Wave Physics Analysis:\n");
        explanation.append("- Price is modeled as a sum of three cosine waves representing short (daily/weekly), medium (monthly), and long-term (quarterly) cycles.\n");
        explanation.append("- Constructive interference (peaks align) indicates strong support/resistance. Destructive interference (peaks cancel) indicates weak levels.\n");
        explanation.append("- The formula used: A_total = A1*cos(w1*t+phi1) + A2*cos(w2*t+phi2) + A3*cos(w3*t+phi3).\n");
        explanation.append("- Support levels are where the wave sum is at a minimum (troughs), resistance at a maximum (peaks), and nodes are equilibrium points.\n");
        explanation.append("- Trading signals are generated when price approaches or breaks interference levels.\n");
        explanation.append("- Future predictions extrapolate the wave sum for the next ")
                   .append(request.getPredictionWeeks()).append(" week(s).\n");
        explanation.append("- Amplitudes represent volatility; higher amplitude means greater price swings.\n");
        explanation.append("- This approach translates wave physics (interference, nodes, amplitude) to financial price behavior, helping identify key levels and timing.\n");

        // Build response with timestamps
        WavePhysicsAnalysisResponse resp = new WavePhysicsAnalysisResponse();
        resp.setSymbol(request.getSymbol());
        resp.setSupportLevels(supportLevels);
        resp.setResistanceLevels(resistanceLevels);
        resp.setNodeLevels(nodeLevels);
        resp.setAmplitudes(Arrays.stream(A).boxed().collect(Collectors.toList()));
        resp.setTradingSignals(tradingSignals);
        resp.setPredictedLevels(predictedLevels);
        resp.setExplanation(explanation.toString());
        
        // Add timestamps for verification
        resp.setAnalysisTimestamp(java.time.LocalDateTime.now().toString());
        resp.setDataRange(request.getStartDate() + " to " + request.getEndDate());
        resp.setDataPoints(data.size());
        
        return resp;
    }
} 