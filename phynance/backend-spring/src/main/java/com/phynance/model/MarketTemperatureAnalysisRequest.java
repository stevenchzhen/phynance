package com.phynance.model;

import java.util.List;

/**
 * Request for market temperature analysis.
 */
public class MarketTemperatureAnalysisRequest {
    private List<String> symbols;
    private String startDate;
    private String endDate;
    private Integer temperatureWindow;
    private Boolean sectorCorrelation;

    public List<String> getSymbols() { return symbols; }
    public void setSymbols(List<String> symbols) { this.symbols = symbols; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Integer getTemperatureWindow() { return temperatureWindow; }
    public void setTemperatureWindow(Integer temperatureWindow) { this.temperatureWindow = temperatureWindow; }
    public Boolean getSectorCorrelation() { return sectorCorrelation; }
    public void setSectorCorrelation(Boolean sectorCorrelation) { this.sectorCorrelation = sectorCorrelation; }
} 