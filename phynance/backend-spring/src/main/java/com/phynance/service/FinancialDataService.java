package com.phynance.service;

import com.phynance.model.MarketData;
import com.phynance.service.provider.YFinanceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class FinancialDataService {
    private final YFinanceProvider yFinanceProvider;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;

    @Autowired
    public FinancialDataService(YFinanceProvider yFinanceProvider) {
        this.yFinanceProvider = yFinanceProvider;
    }

    @Cacheable("marketData")
    // @PreAuthorize("hasAnyRole('TRADER','ANALYST','ADMIN')") // Temporarily disabled for testing
    public MarketData getMarketData(String symbol) {
        String username = getCurrentUsername();
        auditService.logMethodAccess(username, "getMarketData", "FinancialDataService", "SUCCESS");
        
        try {
            return yFinanceProvider.getMarketData(symbol);
        } catch (Exception e) {
            auditService.logError(username, "Failed to fetch market data for " + symbol + ": " + e.getMessage(), null, null);
            throw new RuntimeException("Failed to fetch market data for " + symbol + ": " + e.getMessage());
        }
    }

    /**
     * Fetch historical OHLCV data for a symbol and date range using YFinance.
     */
    // @PreAuthorize("hasAnyRole('VIEWER','TRADER','ANALYST','ADMIN')") // Temporarily disabled for testing
    // @PostAuthorize("returnObject.size() <= (hasRole('ADMIN') ? 10000 : (hasRole('ANALYST') ? 5000 : (hasRole('TRADER') ? 730 : 30)))") // Temporarily disabled for testing
    public List<MarketData> getHistoricalData(String symbol, String startDate, String endDate) {
        String username = getCurrentUsername();
        auditService.logMethodAccess(username, "getHistoricalData", "FinancialDataService", "SUCCESS");
        
        try {
            List<MarketData> data = yFinanceProvider.getHistoricalData(symbol, startDate, endDate);
            
            // Check data access limits
            if (!rateLimitService.checkDataAccessLimit(symbol, data.size())) {
                auditService.logAccessDenied(username, "getHistoricalData", "FinancialDataService", 
                    "Data points " + data.size() + " exceeds limit for role");
                throw new RuntimeException("Data access limit exceeded for your role");
            }
            
            return data;
        } catch (Exception e) {
            auditService.logError(username, "Failed to fetch historical data for " + symbol + ": " + e.getMessage(), null, null);
            throw new RuntimeException("Failed to fetch historical data for " + symbol + ": " + e.getMessage());
        }
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
} 