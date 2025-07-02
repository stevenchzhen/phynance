package com.phynance.service;

import com.phynance.model.HarmonicOscillatorRequest;
import com.phynance.model.PhysicsModelResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for analyzing stock price data using a damped harmonic oscillator model.
 *
 * <p>
 * The price is modeled as: x(t) = A * e^(-γt) * cos(ωt + φ)
 * where:
 *   - A = amplitude (from price volatility)
 *   - γ = damping factor (market friction/resistance)
 *   - ω = angular frequency (market cycle frequency)
 *   - φ = phase shift (trend direction)
 * </p>
 *
 * This service:
 * 1. Fits oscillator parameters to OHLCV data
 * 2. Calculates support/resistance as equilibrium points
 * 3. Predicts reversals at amplitude extremes
 * 4. Generates buy/sell signals on deviation from oscillator
 * 5. Returns predictions for next N trading days
 */
@Service
public class PhysicsModelService {
    /**
     * Analyze stock price data using a damped harmonic oscillator model.
     * @param request HarmonicOscillatorRequest containing OHLCV data and prediction days
     * @return PhysicsModelResult with predictions, support/resistance, and signals
     */
    public PhysicsModelResult analyze(HarmonicOscillatorRequest request) {
        if (request == null || request.getOhlcvData() == null || request.getOhlcvData().isEmpty()) {
            throw new IllegalArgumentException("OHLCV data is required");
        }
        int n = request.getOhlcvData().size();
        List<Double> closes = new ArrayList<>();
        for (HarmonicOscillatorRequest.Ohlcv o : request.getOhlcvData()) {
            closes.add(o.getClose());
        }
        // 1. Estimate amplitude (A) as stddev of closes
        double mean = closes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = closes.stream().mapToDouble(c -> (c - mean) * (c - mean)).sum() / n;
        double amplitude = Math.sqrt(variance);
        // 2. Estimate damping (γ) as a small fraction (e.g., 0.05)
        double damping = 0.05;
        // 3. Estimate frequency (ω) from dominant cycle (use FFT or autocorrelation, here use 2π/period)
        int period = Math.max(5, Math.min(20, n / 3));
        double frequency = 2 * Math.PI / period;
        // 4. Estimate phase (φ) as 0 for simplicity
        double phase = 0;
        // 5. Generate predictions for next N days
        int predDays = Math.max(1, Math.min(10, request.getPredictionDays()));
        List<Double> predictions = new ArrayList<>();
        double lastClose = closes.get(closes.size() - 1);
        for (int t = 1; t <= predDays; t++) {
            double xt = amplitude * Math.exp(-damping * t) * Math.cos(frequency * t + phase) + mean;
            predictions.add(xt);
        }
        // 6. Support/resistance as mean ± amplitude
        List<Double> support = List.of(mean - amplitude);
        List<Double> resistance = List.of(mean + amplitude);
        // 7. Buy/sell signals: buy if prediction > lastClose, sell if < lastClose
        List<String> signals = new ArrayList<>();
        for (double p : predictions) {
            if (p > lastClose) signals.add("BUY");
            else if (p < lastClose) signals.add("SELL");
            else signals.add("HOLD");
        }
        PhysicsModelResult result = new PhysicsModelResult();
        result.setPredictedPrices(predictions);
        result.setSupportLevels(support);
        result.setResistanceLevels(resistance);
        result.setSignals(signals);
        result.setAmplitude(amplitude);
        result.setDamping(damping);
        result.setFrequency(frequency);
        result.setPhase(phase);
        return result;
    }
} 