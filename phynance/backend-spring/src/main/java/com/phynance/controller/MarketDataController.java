package com.phynance.controller;

import com.phynance.model.MarketData;
import com.phynance.service.FinancialDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketDataController {
    private final FinancialDataService financialDataService;

    @Autowired
    public MarketDataController(FinancialDataService financialDataService) {
        this.financialDataService = financialDataService;
    }

    @GetMapping("/api/v1/market-data")
    public MarketData getMarketData(@RequestParam String symbol) {
        return financialDataService.getMarketData(symbol);
    }
} 