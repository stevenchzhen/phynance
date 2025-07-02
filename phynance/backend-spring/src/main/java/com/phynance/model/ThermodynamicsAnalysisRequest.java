package com.phynance.model;

import java.util.List;

/**
 * Request for thermodynamic stock analysis.
 * Includes symbol, date range, and related symbols for sector correlation.
 */
public class ThermodynamicsAnalysisRequest {
    private String symbol;
    private String startDate;
    private String endDate;
    private List<String> relatedSymbols;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<String> getRelatedSymbols() { return relatedSymbols; }
    public void setRelatedSymbols(List<String> relatedSymbols) { this.relatedSymbols = relatedSymbols; }
} 