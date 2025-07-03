package com.phynance.service;

import com.phynance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComprehensiveAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveAnalysisService.class);

    @Autowired private PhysicsModelService physicsModelService;
    @Autowired private ThermodynamicsAnalysisService thermoService;
    @Autowired private WavePhysicsService waveService;
    @Autowired private FinancialDataService financialDataService;
    // For demo, use in-memory map for performance; replace with JPA repo for persistence
    private final Map<String, ComprehensiveAnalysisResponse.ModelPerformance> performanceDb = new HashMap<>();

    public ComprehensiveAnalysisResponse analyze(ComprehensiveAnalysisRequest req) {
        logger.info("Starting comprehensive analysis for {} from {} to {}", req.getSymbol(), req.getStartDate(), req.getEndDate());
        ComprehensiveAnalysisResponse resp = new ComprehensiveAnalysisResponse();
        resp.setSymbol(req.getSymbol());
        try {
            // 1. Fetch historical data
            List<MarketDataDto> ohlcv = financialDataService.getHistoricalData(req.getSymbol(), req.getStartDate(), req.getEndDate());
            // 2. Harmonic Oscillator (PhysicsModelService)
            HarmonicOscillatorRequest hReq = new HarmonicOscillatorRequest();
            List<HarmonicOscillatorRequest.Ohlcv> ohlcvList = ohlcv.stream().map(md -> {
                HarmonicOscillatorRequest.Ohlcv o = new HarmonicOscillatorRequest.Ohlcv();
                o.setDate(md.getTimestamp().toString());
                o.setOpen(md.getOpen());
                o.setHigh(md.getHigh());
                o.setLow(md.getLow());
                o.setClose(md.getClose());
                o.setVolume(md.getVolume());
                return o;
            }).collect(Collectors.toList());
            hReq.setOhlcvData(ohlcvList);
            hReq.setPredictionDays(5);
            PhysicsModelResult hResult = physicsModelService.analyze(hReq);
            HarmonicOscillatorAnalysisResponse hRes = new HarmonicOscillatorAnalysisResponse();
            hRes.setSymbol(req.getSymbol());
            // Fill predictions and signals
            List<HarmonicOscillatorAnalysisResponse.PredictionEntry> hPreds = new ArrayList<>();
            for (int i = 0; i < hResult.getPredictedPrices().size(); i++) {
                HarmonicOscillatorAnalysisResponse.PredictionEntry p = new HarmonicOscillatorAnalysisResponse.PredictionEntry();
                p.setDate("future-day-" + (i + 1));
                p.setPredictedPrice(hResult.getPredictedPrices().get(i));
                p.setSupportLevel(hResult.getSupportLevels().isEmpty() ? null : hResult.getSupportLevels().get(0));
                p.setResistanceLevel(hResult.getResistanceLevels().isEmpty() ? null : hResult.getResistanceLevels().get(0));
                p.setTrendDirection(hResult.getSignals().get(i));
                hPreds.add(p);
            }
            hRes.setPredictions(hPreds);
            // Model metrics (mocked)
            HarmonicOscillatorAnalysisResponse.ModelMetrics hMetrics = new HarmonicOscillatorAnalysisResponse.ModelMetrics();
            hMetrics.setAccuracy(0.78);
            hMetrics.setCorrelation(0.8);
            hMetrics.setRmse(2.0);
            hRes.setModelMetrics(hMetrics);
            resp.setHarmonicOscillator(hRes);

            // 3. Thermodynamics
            ThermodynamicsAnalysisRequest tReq = new ThermodynamicsAnalysisRequest();
            tReq.setSymbol(req.getSymbol());
            tReq.setStartDate(req.getStartDate());
            tReq.setEndDate(req.getEndDate());
            ThermodynamicsAnalysisResponse tRes = thermoService.analyze(tReq, ohlcv, Collections.emptyList());
            resp.setMarketTemperature(tRes);

            // 4. Wave Physics
            WavePhysicsAnalysisRequest wReq = new WavePhysicsAnalysisRequest();
            wReq.setSymbol(req.getSymbol());
            wReq.setStartDate(req.getStartDate());
            wReq.setEndDate(req.getEndDate());
            wReq.setPredictionWeeks(1);
            WavePhysicsAnalysisResponse wRes = waveService.analyze(wReq);
            resp.setWaveInterference(wRes);

            // 5. Ensemble
            if (req.isIncludeEnsemble()) {
                ComprehensiveAnalysisResponse.EnsembleResult ensemble = new ComprehensiveAnalysisResponse.EnsembleResult();
                double hPred = hResult.getPredictedPrices().isEmpty() ? Double.NaN : hResult.getPredictedPrices().get(hResult.getPredictedPrices().size()-1);
                double tPred = tRes.getPredictions() != null && !tRes.getPredictions().isEmpty() ? tRes.getPredictions().get(tRes.getPredictions().size()-1).getPredictedTemperature() : Double.NaN;
                double wPred = wRes.getPredictedLevels() != null && !wRes.getPredictedLevels().isEmpty() ? wRes.getPredictedLevels().get(wRes.getPredictedLevels().size()-1) : Double.NaN;
                List<Double> preds = new ArrayList<>();
                if (!Double.isNaN(hPred)) preds.add(hPred);
                if (!Double.isNaN(tPred)) preds.add(tPred);
                if (!Double.isNaN(wPred)) preds.add(wPred);
                double avg = preds.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
                double min = preds.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
                double max = preds.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
                double stdev = Math.sqrt(preds.stream().mapToDouble(p -> Math.pow(p - avg, 2)).average().orElse(0));
                double confidence = 1.0 - Math.min(stdev / (Math.abs(avg) + 1e-6), 1.0);
                String agreement = stdev < 1 ? "HIGH" : stdev < 3 ? "MEDIUM" : "LOW";
                // Consensus signal: majority vote (BUY/SELL/HOLD)
                List<String> signals = new ArrayList<>();
                if (!hRes.getPredictions().isEmpty()) signals.add(hRes.getPredictions().get(hRes.getPredictions().size()-1).getTrendDirection());
                if (!tRes.getPredictions().isEmpty()) signals.add(tRes.getPredictions().get(tRes.getPredictions().size()-1).getSignal());
                if (wRes.getTradingSignals() != null && !wRes.getTradingSignals().isEmpty()) signals.add(wRes.getTradingSignals().get(wRes.getTradingSignals().size()-1));
                String consensus = signals.stream().filter(s -> s != null).collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                        .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("HOLD");
                // Reasoning
                StringBuilder reasoning = new StringBuilder();
                reasoning.append("All three models analyzed. ");
                reasoning.append("Harmonic: ").append(hMetrics.getAccuracy()).append(", ");
                reasoning.append("Thermo: ").append(tRes.getMetrics() != null ? tRes.getMetrics().getAvgTemperature() : "-").append(", ");
                reasoning.append("Wave: ").append(wRes.getAmplitudes() != null ? wRes.getAmplitudes().stream().map(a -> String.format("%.2f", a)).collect(Collectors.joining(", ")) : "-");
                ensemble.setConsensusSignal(consensus);
                ensemble.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);
                ensemble.setModelAgreement(agreement);
                ensemble.setPredictedPrice(Math.round(avg * 100.0) / 100.0);
                ensemble.setPriceRange(new double[]{Math.round(min * 100.0) / 100.0, Math.round(max * 100.0) / 100.0});
                ensemble.setTimeframe("5-7 days");
                ensemble.setReasoning(reasoning.toString());
                resp.setEnsemble(ensemble);
            }

            // 6. Model performance (mocked for now)
            ComprehensiveAnalysisResponse.ModelPerformance perf = performanceDb.getOrDefault(req.getSymbol(), new ComprehensiveAnalysisResponse.ModelPerformance());
            perf.setHarmonicAccuracy(hMetrics.getAccuracy());
            perf.setTemperatureAccuracy(tRes.getMetrics() != null ? tRes.getMetrics().getAvgTemperature() : 0.71);
            perf.setWaveAccuracy(0.82); // Could be calculated from backtest
            perf.setEnsembleAccuracy((perf.getHarmonicAccuracy() + perf.getTemperatureAccuracy() + perf.getWaveAccuracy()) / 3.0 + 0.03);
            performanceDb.put(req.getSymbol(), perf);
            resp.setModelPerformance(perf);

            logger.info("Comprehensive analysis complete for {}", req.getSymbol());
        } catch (Exception e) {
            logger.error("Error in comprehensive analysis: {}", e.getMessage(), e);
            throw new RuntimeException("Comprehensive analysis failed: " + e.getMessage());
        }
        return resp;
    }
} 