package com.phynance.model;

import java.util.List;
import java.io.Serializable;

public class WavePhysicsAnalysisResponse implements Serializable {
    private String symbol;
    private List<Double> supportLevels;
    private List<Double> resistanceLevels;
    private List<Double> nodeLevels;
    private List<Double> amplitudes;
    private List<String> tradingSignals;
    private List<Double> predictedLevels;
    private String explanation;
    private String analysisTimestamp;
    private String dataRange;
    private int dataPoints;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<Double> getSupportLevels() { return supportLevels; }
    public void setSupportLevels(List<Double> supportLevels) { this.supportLevels = supportLevels; }
    public List<Double> getResistanceLevels() { return resistanceLevels; }
    public void setResistanceLevels(List<Double> resistanceLevels) { this.resistanceLevels = resistanceLevels; }
    public List<Double> getNodeLevels() { return nodeLevels; }
    public void setNodeLevels(List<Double> nodeLevels) { this.nodeLevels = nodeLevels; }
    public List<Double> getAmplitudes() { return amplitudes; }
    public void setAmplitudes(List<Double> amplitudes) { this.amplitudes = amplitudes; }
    public List<String> getTradingSignals() { return tradingSignals; }
    public void setTradingSignals(List<String> tradingSignals) { this.tradingSignals = tradingSignals; }
    public List<Double> getPredictedLevels() { return predictedLevels; }
    public void setPredictedLevels(List<Double> predictedLevels) { this.predictedLevels = predictedLevels; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    public String getDataRange() { return dataRange; }
    public void setDataRange(String dataRange) { this.dataRange = dataRange; }
    public int getDataPoints() { return dataPoints; }
    public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }
} 