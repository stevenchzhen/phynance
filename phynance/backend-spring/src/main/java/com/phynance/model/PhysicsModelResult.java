package com.phynance.model;


import java.util.List;

/**
 * Result of the financial harmonic oscillator analysis.
 */
public class PhysicsModelResult {
    /** Predicted prices for the next 5-10 trading days. */
    private List<Double> predictedPrices;
    /** Calculated support levels. */
    private List<Double> supportLevels;
    /** Calculated resistance levels. */
    private List<Double> resistanceLevels;
    /** Buy/sell/hold signals for each prediction. */
    private List<String> signals;
    /** Oscillator parameters used in the model. */
    private double amplitude;
    private double damping;
    private double frequency;
    private double phase;

    // Getters and setters
    public List<Double> getPredictedPrices() { return predictedPrices; }
    public void setPredictedPrices(List<Double> predictedPrices) { this.predictedPrices = predictedPrices; }
    public List<Double> getSupportLevels() { return supportLevels; }
    public void setSupportLevels(List<Double> supportLevels) { this.supportLevels = supportLevels; }
    public List<Double> getResistanceLevels() { return resistanceLevels; }
    public void setResistanceLevels(List<Double> resistanceLevels) { this.resistanceLevels = resistanceLevels; }
    public List<String> getSignals() { return signals; }
    public void setSignals(List<String> signals) { this.signals = signals; }
    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }
    public double getDamping() { return damping; }
    public void setDamping(double damping) { this.damping = damping; }
    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }
    public double getPhase() { return phase; }
    public void setPhase(double phase) { this.phase = phase; }
} 