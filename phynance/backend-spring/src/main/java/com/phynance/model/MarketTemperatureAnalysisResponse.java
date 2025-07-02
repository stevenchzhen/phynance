package com.phynance.model;

import java.util.List;

/**
 * Response for market temperature analysis.
 */
public class MarketTemperatureAnalysisResponse {
    private MarketTemperature marketTemperature;
    private List<SymbolTemperature> symbols;
    private List<PhaseTransition> phaseTransitions;
    private List<MarketPrediction> predictions;

    public MarketTemperature getMarketTemperature() { return marketTemperature; }
    public void setMarketTemperature(MarketTemperature marketTemperature) { this.marketTemperature = marketTemperature; }
    public List<SymbolTemperature> getSymbols() { return symbols; }
    public void setSymbols(List<SymbolTemperature> symbols) { this.symbols = symbols; }
    public List<PhaseTransition> getPhaseTransitions() { return phaseTransitions; }
    public void setPhaseTransitions(List<PhaseTransition> phaseTransitions) { this.phaseTransitions = phaseTransitions; }
    public List<MarketPrediction> getPredictions() { return predictions; }
    public void setPredictions(List<MarketPrediction> predictions) { this.predictions = predictions; }

    public static class MarketTemperature {
        private Double current;
        private Double average;
        private String trend; // HEATING|COOLING|STABLE
        private String phaseState; // LIQUID|GAS|SOLID
        public Double getCurrent() { return current; }
        public void setCurrent(Double current) { this.current = current; }
        public Double getAverage() { return average; }
        public void setAverage(Double average) { this.average = average; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
        public String getPhaseState() { return phaseState; }
        public void setPhaseState(String phaseState) { this.phaseState = phaseState; }
    }
    public static class SymbolTemperature {
        private String symbol;
        private Double temperature;
        private Double heatFlow;
        private Double equilibriumPrice;
        private String thermalSignal;
        private Double confidence;
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Double getHeatFlow() { return heatFlow; }
        public void setHeatFlow(Double heatFlow) { this.heatFlow = heatFlow; }
        public Double getEquilibriumPrice() { return equilibriumPrice; }
        public void setEquilibriumPrice(Double equilibriumPrice) { this.equilibriumPrice = equilibriumPrice; }
        public String getThermalSignal() { return thermalSignal; }
        public void setThermalSignal(String thermalSignal) { this.thermalSignal = thermalSignal; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }
    public static class PhaseTransition {
        private String date;
        private String type; // BULL_TO_BEAR, etc.
        private Double temperatureChange;
        private List<String> affectedSymbols;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Double getTemperatureChange() { return temperatureChange; }
        public void setTemperatureChange(Double temperatureChange) { this.temperatureChange = temperatureChange; }
        public List<String> getAffectedSymbols() { return affectedSymbols; }
        public void setAffectedSymbols(List<String> affectedSymbols) { this.affectedSymbols = affectedSymbols; }
    }
    public static class MarketPrediction {
        private String date;
        private Double marketTemperature;
        private String expectedVolatility;
        private String recommendedAction;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getMarketTemperature() { return marketTemperature; }
        public void setMarketTemperature(Double marketTemperature) { this.marketTemperature = marketTemperature; }
        public String getExpectedVolatility() { return expectedVolatility; }
        public void setExpectedVolatility(String expectedVolatility) { this.expectedVolatility = expectedVolatility; }
        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }
} 