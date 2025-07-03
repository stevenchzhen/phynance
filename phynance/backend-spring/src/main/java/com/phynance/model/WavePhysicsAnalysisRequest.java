package com.phynance.model;

public class WavePhysicsAnalysisRequest {
    private String symbol;
    private String startDate;
    private String endDate;
    private int predictionWeeks;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getPredictionWeeks() { return predictionWeeks; }
    public void setPredictionWeeks(int predictionWeeks) { this.predictionWeeks = predictionWeeks; }
} 