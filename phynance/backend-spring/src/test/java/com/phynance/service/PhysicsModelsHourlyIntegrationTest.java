package com.phynance.service;

import com.phynance.model.MarketDataDto;
import com.phynance.service.provider.YFinanceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.phynance.model.HarmonicOscillatorRequest;
import com.phynance.model.ThermodynamicsAnalysisRequest;
import com.phynance.model.WavePhysicsAnalysisRequest;
import com.phynance.model.ThermodynamicsAnalysisResponse;
import com.phynance.model.PhysicsModelResult;
import com.phynance.model.WavePhysicsAnalysisResponse;
import java.util.Collections;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class PhysicsModelsHourlyIntegrationTest {

    @Autowired
    private YFinanceProvider yFinanceProvider;
    @Autowired
    private PhysicsModelService physicsModelService;
    @Autowired
    private ThermodynamicsAnalysisService thermodynamicsAnalysisService;
    @Autowired
    private WavePhysicsService wavePhysicsService;

    @Test
    public void testHourlyDataAndPhysicsModels() {
        String[] symbols = {"AAPL", "TSLA", "META", "GOOG", "NVDA", "NFLX", "MSTR"};
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        for (String symbol : symbols) {
            System.out.println("\n==============================");
            System.out.println("Testing symbol: " + symbol);
            List<MarketDataDto> data = yFinanceProvider.getHourlyData(symbol, start.toString(), end.toString());
            System.out.println("Fetched " + data.size() + " hourly data points for " + symbol);
            if (data.size() < 60) {
                System.out.println("Warning: Less than 60 data points fetched. Test may not be representative.");
            }
            System.out.println("First 3 data points:");
            for (int i = 0; i < Math.min(3, data.size()); i++) {
                MarketDataDto md = data.get(i);
                System.out.printf("  %s: O=%.2f, H=%.2f, L=%.2f, C=%.2f, V=%d\n",
                        md.getTimestamp(), md.getOpen(), md.getHigh(), md.getLow(), md.getClose(), md.getVolume());
            }

            // Harmonic Oscillator Model
            HarmonicOscillatorRequest hoReq = new HarmonicOscillatorRequest();
            List<HarmonicOscillatorRequest.Ohlcv> ohlcvList = data.stream().map(md -> {
                HarmonicOscillatorRequest.Ohlcv o = new HarmonicOscillatorRequest.Ohlcv();
                o.setDate(md.getTimestamp().toString());
                o.setOpen(md.getOpen());
                o.setHigh(md.getHigh());
                o.setLow(md.getLow());
                o.setClose(md.getClose());
                o.setVolume(md.getVolume());
                return o;
            }).toList();
            hoReq.setOhlcvData(ohlcvList);
            hoReq.setPredictionDays(5);
            PhysicsModelResult hoResult = physicsModelService.analyze(hoReq);
            System.out.println("\n=== HARMONIC OSCILLATOR MODEL RESULT ===");
            System.out.println("Predicted Prices: " + hoResult.getPredictedPrices());
            System.out.println("Support Levels: " + hoResult.getSupportLevels());
            System.out.println("Resistance Levels: " + hoResult.getResistanceLevels());
            System.out.println("Signals: " + hoResult.getSignals());
            System.out.println("Amplitude: " + hoResult.getAmplitude());
            System.out.println("Damping: " + hoResult.getDamping());
            System.out.println("Frequency: " + hoResult.getFrequency());

            // Thermodynamics Model
            ThermodynamicsAnalysisRequest thermoReq = new ThermodynamicsAnalysisRequest();
            thermoReq.setSymbol(symbol);
            thermoReq.setStartDate(start.toString());
            thermoReq.setEndDate(end.toString());
            ThermodynamicsAnalysisResponse thermoResult = thermodynamicsAnalysisService.analyze(thermoReq, data, java.util.Collections.emptyList());
            System.out.println("\n=== THERMODYNAMICS MODEL RESULT ===");
            System.out.println("Symbol: " + thermoResult.getSymbol());
            System.out.println("Temperature Trends Count: " + (thermoResult.getTemperatureTrends() != null ? thermoResult.getTemperatureTrends().size() : 0));
            System.out.println("Phase Transitions Count: " + (thermoResult.getPhaseTransitions() != null ? thermoResult.getPhaseTransitions().size() : 0));
            System.out.println("Predictions Count: " + (thermoResult.getPredictions() != null ? thermoResult.getPredictions().size() : 0));
            System.out.println("Avg Temperature: " + (thermoResult.getMetrics() != null ? thermoResult.getMetrics().getAvgTemperature() : "N/A"));
            System.out.println("Entropy: " + (thermoResult.getMetrics() != null ? thermoResult.getMetrics().getEntropy() : "N/A"));
            System.out.println("Heat Capacity: " + (thermoResult.getMetrics() != null ? thermoResult.getMetrics().getHeatCapacity() : "N/A"));

            // Wave Physics Model
            WavePhysicsAnalysisRequest waveReq = new WavePhysicsAnalysisRequest();
            waveReq.setSymbol(symbol);
            waveReq.setStartDate(start.toString());
            waveReq.setEndDate(end.toString());
            waveReq.setPredictionWeeks(1);
            WavePhysicsAnalysisResponse waveResult = wavePhysicsService.analyze(waveReq, data);
            System.out.println("\n=== WAVE PHYSICS MODEL RESULT ===");
            System.out.println("Symbol: " + waveResult.getSymbol());
            System.out.println("Support Levels: " + waveResult.getSupportLevels());
            System.out.println("Resistance Levels: " + waveResult.getResistanceLevels());
            System.out.println("Node Levels: " + waveResult.getNodeLevels());
            System.out.println("Amplitudes: " + waveResult.getAmplitudes());
            System.out.println("Trading Signals Count: " + (waveResult.getTradingSignals() != null ? waveResult.getTradingSignals().size() : 0));
            System.out.println("Predicted Levels: " + waveResult.getPredictedLevels());
            System.out.println("Explanation: " + (waveResult.getExplanation() != null ? waveResult.getExplanation().substring(0, Math.min(200, waveResult.getExplanation().length())) + "..." : "N/A"));
        }
    }
} 