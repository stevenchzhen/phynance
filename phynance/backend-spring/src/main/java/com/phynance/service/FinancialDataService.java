package com.phynance.service;

import com.phynance.model.MarketDataDto;
import com.phynance.service.provider.YFinanceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialDataService {
    private final YFinanceProvider yFinanceProvider;

    @Autowired
    public FinancialDataService(YFinanceProvider yFinanceProvider) {
        this.yFinanceProvider = yFinanceProvider;
    }

    @Cacheable("marketData")
    public MarketDataDto getMarketData(String symbol) {
        try {
            return yFinanceProvider.getMarketData(symbol);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch market data for " + symbol + ": " + e.getMessage());
        }
    }

    /**
     * Fetch historical OHLCV data for a symbol and date range using YFinance.
     */
    public List<MarketDataDto> getHistoricalData(String symbol, String startDate, String endDate) {
        try {
            return yFinanceProvider.getHistoricalData(symbol, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch historical data for " + symbol + ": " + e.getMessage());
        }
    }
} 