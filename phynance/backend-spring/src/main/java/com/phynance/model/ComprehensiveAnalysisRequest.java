package com.phynance.model;

public class ComprehensiveAnalysisRequest {
    private String symbol;
    private String startDate;
    private String endDate;
    private boolean includeEnsemble;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public boolean isIncludeEnsemble() { return includeEnsemble; }
    public void setIncludeEnsemble(boolean includeEnsemble) { this.includeEnsemble = includeEnsemble; }
} 