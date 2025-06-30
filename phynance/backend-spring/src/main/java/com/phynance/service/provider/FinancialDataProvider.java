package com.phynance.service.provider;

import com.phynance.model.MarketDataDto;

public interface FinancialDataProvider {
    MarketDataDto getMarketData(String symbol) throws Exception;
    String getName();
} 