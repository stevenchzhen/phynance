package com.phynance.service;

import com.phynance.model.MarketTemperatureAnalysisRequest;
import com.phynance.model.MarketTemperatureAnalysisResponse;
import com.phynance.model.MarketDataDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for market temperature analysis using thermodynamic analogies.
 *
 * - Market temperature: average kinetic energy (volatility) of price movements
 * - Heat flow: price movement correlation between stocks
 * - Phase transitions: regime changes (bull/bear)
 * - Phase state: market regime (solid = stable, liquid = volatile, gas = chaotic)
 */
@Service
public class MarketTemperatureAnalysisService {
    public MarketTemperatureAnalysisResponse analyze(MarketTemperatureAnalysisRequest request, List<List<MarketDataDto>> allOhlcv) {
        int window = request.getTemperatureWindow() != null ? request.getTemperatureWindow() : 20;
        List<String> symbols = request.getSymbols();
        List<MarketTemperatureAnalysisResponse.SymbolTemperature> symbolTemps = new ArrayList<>();
        List<Double> allTemps = new ArrayList<>();
        // 1. Calculate temperature for each symbol
        for (int i = 0; i < symbols.size(); i++) {
            List<MarketDataDto> ohlcv = allOhlcv.get(i);
            double temp = calcTemperature(ohlcv, window);
            allTemps.add(temp);
            MarketTemperatureAnalysisResponse.SymbolTemperature st = new MarketTemperatureAnalysisResponse.SymbolTemperature();
            st.setSymbol(symbols.get(i));
            st.setTemperature(temp);
            st.setEquilibriumPrice(ohlcv.isEmpty() ? null : ohlcv.get(ohlcv.size()-1).getClose());
            st.setThermalSignal(temp < 50 ? "COOLING_BUY" : temp > 100 ? "HEATING_SELL" : "STABLE_HOLD");
            st.setConfidence(0.7); // Placeholder
            symbolTemps.add(st);
        }
        // 2. Calculate market temperature (average)
        double avgTemp = allTemps.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double currentTemp = allTemps.isEmpty() ? 0 : allTemps.get(allTemps.size()-1);
        String trend = currentTemp > avgTemp ? "HEATING" : currentTemp < avgTemp ? "COOLING" : "STABLE";
        String phaseState = currentTemp > 120 ? "GAS" : currentTemp > 80 ? "LIQUID" : "SOLID";
        MarketTemperatureAnalysisResponse.MarketTemperature marketTemp = new MarketTemperatureAnalysisResponse.MarketTemperature();
        marketTemp.setCurrent(currentTemp);
        marketTemp.setAverage(avgTemp);
        marketTemp.setTrend(trend);
        marketTemp.setPhaseState(phaseState);
        // 3. Calculate heat flow (correlation) if requested
        if (Boolean.TRUE.equals(request.getSectorCorrelation()) && symbols.size() > 1) {
            try {
                for (int i = 0; i < symbols.size(); i++) {
                    double heatFlow = 0;
                    for (int j = 0; j < symbols.size(); j++) {
                        if (i == j) continue;
                        heatFlow += calcCorrelation(allOhlcv.get(i), allOhlcv.get(j));
                    }
                    symbolTemps.get(i).setHeatFlow(heatFlow);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error calculating sector correlation: " + e.getMessage());
            }
        }
        // 4. Detect phase transitions (mocked for now)
        List<MarketTemperatureAnalysisResponse.PhaseTransition> transitions = new ArrayList<>();
        if (currentTemp < avgTemp - 15) {
            MarketTemperatureAnalysisResponse.PhaseTransition pt = new MarketTemperatureAnalysisResponse.PhaseTransition();
            pt.setDate(request.getEndDate());
            pt.setType("BULL_TO_BEAR");
            pt.setTemperatureChange(currentTemp - avgTemp);
            pt.setAffectedSymbols(symbols);
            transitions.add(pt);
        }
        // 5. Predictions (mocked)
        List<MarketTemperatureAnalysisResponse.MarketPrediction> preds = new ArrayList<>();
        MarketTemperatureAnalysisResponse.MarketPrediction pred = new MarketTemperatureAnalysisResponse.MarketPrediction();
        pred.setDate(request.getEndDate());
        pred.setMarketTemperature(currentTemp + 2); // mock
        pred.setExpectedVolatility(currentTemp > 100 ? "HIGH" : currentTemp > 70 ? "MEDIUM" : "LOW");
        pred.setRecommendedAction(currentTemp > 100 ? "REDUCE_POSITIONS" : currentTemp < 60 ? "ADD_POSITIONS" : "HOLD");
        preds.add(pred);
        // 6. Build response
        MarketTemperatureAnalysisResponse resp = new MarketTemperatureAnalysisResponse();
        resp.setMarketTemperature(marketTemp);
        resp.setSymbols(symbolTemps);
        resp.setPhaseTransitions(transitions);
        resp.setPredictions(preds);
        return resp;
    }

    /**
     * Calculate market temperature as average squared price change over window.
     */
    private double calcTemperature(List<MarketDataDto> ohlcv, int window) {
        if (ohlcv == null || ohlcv.size() < 2) return 0;
        int n = Math.min(window, ohlcv.size() - 1);
        double sum = 0;
        for (int i = ohlcv.size() - n; i < ohlcv.size(); i++) {
            double dP = ohlcv.get(i).getClose() - ohlcv.get(i-1).getClose();
            sum += dP * dP;
        }
        return sum / n;
    }

    /**
     * Calculate correlation (heat flow analogy) between two symbols' price changes.
     * Returns Pearson correlation coefficient.
     */
    private double calcCorrelation(List<MarketDataDto> a, List<MarketDataDto> b) {
        int n = Math.min(a.size(), b.size());
        if (n < 2) return 0;
        List<Double> da = new ArrayList<>();
        List<Double> db = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            da.add(a.get(i).getClose() - a.get(i-1).getClose());
            db.add(b.get(i).getClose() - b.get(i-1).getClose());
        }
        double meanA = da.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanB = db.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double num = 0, denomA = 0, denomB = 0;
        for (int i = 0; i < da.size(); i++) {
            double x = da.get(i) - meanA;
            double y = db.get(i) - meanB;
            num += x * y;
            denomA += x * x;
            denomB += y * y;
        }
        if (denomA == 0 || denomB == 0) return 0;
        return num / Math.sqrt(denomA * denomB);
    }
} 