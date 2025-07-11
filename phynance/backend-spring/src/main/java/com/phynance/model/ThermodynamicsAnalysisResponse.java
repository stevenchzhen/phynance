package com.phynance.model;

import java.util.List;
import java.io.Serializable;

/**
 * Response for thermodynamic stock analysis.
 */
public class ThermodynamicsAnalysisResponse implements Serializable {
    private String symbol;
    private List<TemperatureTrend> temperatureTrends;
    private List<PhaseTransitionAlert> phaseTransitions;
    private List<ThermalPrediction> predictions;
    private ThermoMetrics metrics;
    private String analysisTimestamp;
    private String dataRange;
    private int dataPoints;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<TemperatureTrend> getTemperatureTrends() { return temperatureTrends; }
    public void setTemperatureTrends(List<TemperatureTrend> temperatureTrends) { this.temperatureTrends = temperatureTrends; }
    public List<PhaseTransitionAlert> getPhaseTransitions() { return phaseTransitions; }
    public void setPhaseTransitions(List<PhaseTransitionAlert> phaseTransitions) { this.phaseTransitions = phaseTransitions; }
    public List<ThermalPrediction> getPredictions() { return predictions; }
    public void setPredictions(List<ThermalPrediction> predictions) { this.predictions = predictions; }
    public ThermoMetrics getMetrics() { return metrics; }
    public void setMetrics(ThermoMetrics metrics) { this.metrics = metrics; }
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    public String getDataRange() { return dataRange; }
    public void setDataRange(String dataRange) { this.dataRange = dataRange; }
    public int getDataPoints() { return dataPoints; }
    public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }

    public static class TemperatureTrend implements Serializable {
        private String date;
        private Double temperature;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
    }
    public static class PhaseTransitionAlert implements Serializable {
        private String date;
        private String type; // e.g., "BULL_TO_BEAR", "BEAR_TO_BULL"
        private String description;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    public static class ThermalPrediction implements Serializable {
        private String date;
        private String signal; // BUY, SELL, HOLD
        private Double predictedTemperature;
        private String comment;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public Double getPredictedTemperature() { return predictedTemperature; }
        public void setPredictedTemperature(Double predictedTemperature) { this.predictedTemperature = predictedTemperature; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    public static class ThermoMetrics implements Serializable {
        private Double avgTemperature;
        private Double entropy;
        private Double heatCapacity;
        public Double getAvgTemperature() { return avgTemperature; }
        public void setAvgTemperature(Double avgTemperature) { this.avgTemperature = avgTemperature; }
        public Double getEntropy() { return entropy; }
        public void setEntropy(Double entropy) { this.entropy = entropy; }
        public Double getHeatCapacity() { return heatCapacity; }
        public void setHeatCapacity(Double heatCapacity) { this.heatCapacity = heatCapacity; }
    }
} 