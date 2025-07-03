package com.phynance.service.provider;

import com.google.common.util.concurrent.RateLimiter;
import com.phynance.config.ApiProviderConfig;
import com.phynance.model.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Component
public class AlphaVantageProvider implements FinancialDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageProvider.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ApiProviderConfig.AlphaVantageConfig config;
    private final RateLimiter rateLimiter;

    public AlphaVantageProvider(ApiProviderConfig apiConfig) {
        this.config = apiConfig.getAlphaVantage();
        this.rateLimiter = RateLimiter.create(config.getRateLimitPerSecond());
    }

    @Override
    public MarketData getMarketData(String symbol) throws Exception {
        rateLimiter.acquire();
        String url = UriComponentsBuilder.fromHttpUrl(config.getUrl())
                .queryParam("function", "TIME_SERIES_INTRADAY")
                .queryParam("symbol", symbol)
                .queryParam("interval", "1min")
                .queryParam("apikey", config.getApiKey())
                .toUriString();
        int maxRetries = 5;
        long backoff = 1000; // 1 second
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (1min)");
                if (timeSeries == null || timeSeries.isEmpty()) throw new RuntimeException("No data from Alpha Vantage");
                String latestKey = timeSeries.keySet().iterator().next();
                Map<String, String> latest = (Map<String, String>) timeSeries.get(latestKey);
                MarketData dto = new MarketData();
                dto.setSymbol(symbol);
                dto.setTimestamp(Instant.parse(latestKey.replace(" ", "T") + "Z"));
                dto.setOpen(Double.parseDouble(latest.get("1. open")));
                dto.setHigh(Double.parseDouble(latest.get("2. high")));
                dto.setLow(Double.parseDouble(latest.get("3. low")));
                dto.setClose(Double.parseDouble(latest.get("4. close")));
                dto.setVolume(Long.parseLong(latest.get("5. volume")));
                return dto;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn("AlphaVantage 429 rate limit hit, retry {}/{} after {}ms", attempt, maxRetries, backoff);
                    Thread.sleep(backoff);
                    backoff *= 2;
                } else {
                    logger.error("AlphaVantage HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw e;
                }
            } catch (ResourceAccessException e) {
                logger.warn("AlphaVantage network timeout, retry {}/{} after {}ms", attempt, maxRetries, backoff);
                Thread.sleep(backoff);
                backoff *= 2;
            } catch (Exception e) {
                logger.warn("AlphaVantage error ({}), retry {}/{} after {}ms: {}", e.getClass().getSimpleName(), attempt, maxRetries, backoff, e.getMessage());
                if (attempt == maxRetries) throw e;
                Thread.sleep(backoff);
                backoff *= 2;
            }
        }
        throw new RuntimeException("AlphaVantage retries exhausted");
    }

    @Override
    public String getName() { return "AlphaVantage"; }
} 