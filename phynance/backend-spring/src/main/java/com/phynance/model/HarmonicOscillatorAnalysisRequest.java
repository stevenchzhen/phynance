package com.phynance.model;

/**
 * Request for harmonic oscillator stock analysis.
 */
public class HarmonicOscillatorAnalysisRequest {
    private String symbol;
    private String startDate;
    private String endDate;
    private Double dampingFactor;
    private Double frequency;
    private Integer predictionDays;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Double getDampingFactor() { return dampingFactor; }
    public void setDampingFactor(Double dampingFactor) { this.dampingFactor = dampingFactor; }
    public Double getFrequency() { return frequency; }
    public void setFrequency(Double frequency) { this.frequency = frequency; }
    public Integer getPredictionDays() { return predictionDays; }
    public void setPredictionDays(Integer predictionDays) { this.predictionDays = predictionDays; }
} 