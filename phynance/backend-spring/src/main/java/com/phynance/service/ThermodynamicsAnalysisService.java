package com.phynance.service;

import com.phynance.model.ThermodynamicsAnalysisRequest;
import com.phynance.model.ThermodynamicsAnalysisResponse;
import com.phynance.model.MarketDataDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for thermodynamic stock market analysis.
 *
 * <p>
 * Applies statistical mechanics and thermodynamics to stock price behavior:
 * - Market 'temperature' = average kinetic energy of price movements
 * - Heat transfer = sector correlation
 * - Thermal equilibrium = price stability zones
 * - Phase transitions = bull/bear market changes
 * - Entropy, heat capacity, and critical thresholds for market regime shifts
 * </p>
 */
@Service
public class ThermodynamicsAnalysisService {
    /**
     * Analyze stock data using thermodynamic principles.
     * @param request ThermodynamicsAnalysisRequest
     * @param ohlcvData List of MarketDataDto for the main symbol
     * @param relatedOhlcv List of List<MarketDataDto> for related symbols (sector correlation)
     * @return ThermodynamicsAnalysisResponse
     */
    public ThermodynamicsAnalysisResponse analyze(ThermodynamicsAnalysisRequest request, List<MarketDataDto> ohlcvData, List<List<MarketDataDto>> relatedOhlcv) {
        List<ThermodynamicsAnalysisResponse.TemperatureTrend> tempTrends = new ArrayList<>();
        List<ThermodynamicsAnalysisResponse.PhaseTransitionAlert> phaseTransitions = new ArrayList<>();
        List<ThermodynamicsAnalysisResponse.ThermalPrediction> predictions = new ArrayList<>();
        double sumTemp = 0;
        double prevTemp = 0;
        double entropy = 0;
        double heatCapacity = 0;
        // 1. Calculate market temperature for each day
        for (int i = 1; i < ohlcvData.size(); i++) {
            double dP = ohlcvData.get(i).getClose() - ohlcvData.get(i-1).getClose();
            double temp = dP * dP; // T = ΔP² (kinetic energy analogy)
            sumTemp += temp;
            ThermodynamicsAnalysisResponse.TemperatureTrend trend = new ThermodynamicsAnalysisResponse.TemperatureTrend();
            trend.setDate(ohlcvData.get(i).getTimestamp().toString());
            trend.setTemperature(temp);
            tempTrends.add(trend);
            // 2. Detect phase transitions (bull/bear) using critical temp
            if (i > 1) {
                double deltaT = temp - prevTemp;
                if (temp > 2 * average(tempTrends)) {
                    ThermodynamicsAnalysisResponse.PhaseTransitionAlert alert = new ThermodynamicsAnalysisResponse.PhaseTransitionAlert();
                    alert.setDate(trend.getDate());
                    alert.setType("OVERHEAT");
                    alert.setDescription("Market temperature exceeds critical threshold: possible crash risk");
                    phaseTransitions.add(alert);
                } else if (temp < 0.5 * average(tempTrends)) {
                    ThermodynamicsAnalysisResponse.PhaseTransitionAlert alert = new ThermodynamicsAnalysisResponse.PhaseTransitionAlert();
                    alert.setDate(trend.getDate());
                    alert.setType("SUPERCOOL");
                    alert.setDescription("Market temperature below critical threshold: possible rally");
                    phaseTransitions.add(alert);
                }
                // Entropy: measure of disorder (stddev of temp)
                entropy += Math.abs(deltaT);
                // Heat capacity: response to 'news' (here, price jumps)
                heatCapacity += Math.abs(dP);
            }
            prevTemp = temp;
        }
        // 3. Generate trading signals based on temperature
        for (ThermodynamicsAnalysisResponse.TemperatureTrend trend : tempTrends) {
            ThermodynamicsAnalysisResponse.ThermalPrediction pred = new ThermodynamicsAnalysisResponse.ThermalPrediction();
            pred.setDate(trend.getDate());
            pred.setPredictedTemperature(trend.getTemperature());
            if (trend.getTemperature() > 2 * average(tempTrends)) {
                pred.setSignal("SELL");
                pred.setComment("Overheated market: potential crash");
            } else if (trend.getTemperature() < 0.5 * average(tempTrends)) {
                pred.setSignal("BUY");
                pred.setComment("Supercooled market: potential rally");
            } else {
                pred.setSignal("HOLD");
                pred.setComment("Thermal equilibrium: sideways movement");
            }
            predictions.add(pred);
        }
        // 4. Fill metrics
        ThermodynamicsAnalysisResponse.ThermoMetrics metrics = new ThermodynamicsAnalysisResponse.ThermoMetrics();
        metrics.setAvgTemperature(average(tempTrends));
        metrics.setEntropy(entropy / tempTrends.size());
        metrics.setHeatCapacity(heatCapacity / tempTrends.size());
        // 5. Build response
        ThermodynamicsAnalysisResponse resp = new ThermodynamicsAnalysisResponse();
        resp.setSymbol(request.getSymbol());
        resp.setTemperatureTrends(tempTrends);
        resp.setPhaseTransitions(phaseTransitions);
        resp.setPredictions(predictions);
        resp.setMetrics(metrics);
        return resp;
    }

    /**
     * Calculate average temperature from trends.
     */
    private double average(List<ThermodynamicsAnalysisResponse.TemperatureTrend> trends) {
        if (trends.isEmpty()) return 0;
        return trends.stream().mapToDouble(ThermodynamicsAnalysisResponse.TemperatureTrend::getTemperature).average().orElse(0);
    }
} 