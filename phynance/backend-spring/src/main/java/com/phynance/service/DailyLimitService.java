package com.phynance.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DailyLimitService {
    private static final int ALPHA_VANTAGE_DAILY_LIMIT = 5;
    private static final int TWELVE_DATA_DAILY_LIMIT = 8;

    private int alphaVantageCount = 0;
    private int twelveDataCount = 0;

    public synchronized boolean canUseAlphaVantage() {
        return alphaVantageCount < ALPHA_VANTAGE_DAILY_LIMIT;
    }

    public synchronized void incrementAlphaVantage() {
        alphaVantageCount++;
    }

    public synchronized boolean canUseTwelveData() {
        return twelveDataCount < TWELVE_DATA_DAILY_LIMIT;
    }

    public synchronized void incrementTwelveData() {
        twelveDataCount++;
    }

    // Reset counts at midnight every day
    @Scheduled(cron = "0 0 0 * * *")
    public synchronized void resetCounts() {
        alphaVantageCount = 0;
        twelveDataCount = 0;
    }
} 