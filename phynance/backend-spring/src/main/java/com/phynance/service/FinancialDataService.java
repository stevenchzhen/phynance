package com.phynance.service;

import com.phynance.model.MarketDataDto;
import com.phynance.service.provider.AlphaVantageProvider;
import com.phynance.service.provider.TwelveDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FinancialDataService {
    private final AlphaVantageProvider alphaVantageProvider;
    private final TwelveDataProvider twelveDataProvider;
    private final DailyLimitService dailyLimitService;

    @Autowired
    public FinancialDataService(
        AlphaVantageProvider alphaVantageProvider,
        TwelveDataProvider twelveDataProvider,
        DailyLimitService dailyLimitService
    ) {
        this.alphaVantageProvider = alphaVantageProvider;
        this.twelveDataProvider = twelveDataProvider;
        this.dailyLimitService = dailyLimitService;
    }

    @Cacheable("marketData")
    public MarketDataDto getMarketData(String symbol) {
        if (dailyLimitService.canUseAlphaVantage()) {
            try {
                MarketDataDto dto = alphaVantageProvider.getMarketData(symbol);
                dailyLimitService.incrementAlphaVantage();
                return dto;
            } catch (Exception e) {
                // log and fall through to next provider
            }
        }
        if (dailyLimitService.canUseTwelveData()) {
            try {
                MarketDataDto dto = twelveDataProvider.getMarketData(symbol);
                dailyLimitService.incrementTwelveData();
                return dto;
            } catch (Exception e) {
                // log and throw if both fail
            }
        }
        throw new RuntimeException("All providers failed or daily limits reached");
    }
} 