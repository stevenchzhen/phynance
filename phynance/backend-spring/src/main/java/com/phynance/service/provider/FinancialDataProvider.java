package com.phynance.service.provider;

import com.phynance.model.MarketData;

public interface FinancialDataProvider {
    MarketData getMarketData(String symbol) throws Exception;
    String getName();
} 