package com.phynance.model;

import java.util.List;
import java.io.Serializable;

/**
 * Response for harmonic oscillator stock analysis.
 */
public class HarmonicOscillatorAnalysisResponse implements Serializable {
    private String symbol;
    private List<AnalysisEntry> analysis;
    private List<PredictionEntry> predictions;
    private ModelMetrics modelMetrics;
    private String analysisTimestamp;
    private String dataRange;
    private int dataPoints;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<AnalysisEntry> getAnalysis() { return analysis; }
    public void setAnalysis(List<AnalysisEntry> analysis) { this.analysis = analysis; }
    public List<PredictionEntry> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionEntry> predictions) { this.predictions = predictions; }
    public ModelMetrics getModelMetrics() { return modelMetrics; }
    public void setModelMetrics(ModelMetrics modelMetrics) { this.modelMetrics = modelMetrics; }
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    public String getDataRange() { return dataRange; }
    public void setDataRange(String dataRange) { this.dataRange = dataRange; }
    public int getDataPoints() { return dataPoints; }
    public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }

    public static class AnalysisEntry implements Serializable {
        private String date;
        private Double actualPrice;
        private Double oscillatorValue;
        private Double amplitude;
        private Double phase;
        private String signal;
        private Double confidence;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getActualPrice() { return actualPrice; }
        public void setActualPrice(Double actualPrice) { this.actualPrice = actualPrice; }
        public Double getOscillatorValue() { return oscillatorValue; }
        public void setOscillatorValue(Double oscillatorValue) { this.oscillatorValue = oscillatorValue; }
        public Double getAmplitude() { return amplitude; }
        public void setAmplitude(Double amplitude) { this.amplitude = amplitude; }
        public Double getPhase() { return phase; }
        public void setPhase(Double phase) { this.phase = phase; }
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }

    public static class PredictionEntry implements Serializable {
        private String date;
        private Double predictedPrice;
        private Double supportLevel;
        private Double resistanceLevel;
        private String trendDirection;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getPredictedPrice() { return predictedPrice; }
        public void setPredictedPrice(Double predictedPrice) { this.predictedPrice = predictedPrice; }
        public Double getSupportLevel() { return supportLevel; }
        public void setSupportLevel(Double supportLevel) { this.supportLevel = supportLevel; }
        public Double getResistanceLevel() { return resistanceLevel; }
        public void setResistanceLevel(Double resistanceLevel) { this.resistanceLevel = resistanceLevel; }
        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }
    }

    public static class ModelMetrics implements Serializable {
        private Double accuracy;
        private Double correlation;
        private Double rmse;
        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
        public Double getCorrelation() { return correlation; }
        public void setCorrelation(Double correlation) { this.correlation = correlation; }
        public Double getRmse() { return rmse; }
        public void setRmse(Double rmse) { this.rmse = rmse; }
    }
} 