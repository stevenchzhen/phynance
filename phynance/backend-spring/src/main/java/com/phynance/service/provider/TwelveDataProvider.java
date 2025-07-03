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
import java.util.List;
import java.util.Map;

@Component
public class TwelveDataProvider implements FinancialDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(TwelveDataProvider.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ApiProviderConfig.TwelveDataConfig config;
    private final RateLimiter rateLimiter;

    public TwelveDataProvider(ApiProviderConfig apiConfig) {
        this.config = apiConfig.getTwelveData();
        this.rateLimiter = RateLimiter.create(config.getRateLimitPerSecond());
    }

    @Override
    public MarketData getMarketData(String symbol) throws Exception {
        rateLimiter.acquire();
        String url = UriComponentsBuilder.fromHttpUrl(config.getUrl() + "/time_series")
                .queryParam("symbol", symbol)
                .queryParam("interval", "1min")
                .queryParam("apikey", config.getApiKey())
                .toUriString();
        int maxRetries = 5;
        long backoff = 1000; // 1 second
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                List<Map<String, String>> values = (List<Map<String, String>>) response.get("values");
                if (values == null || values.isEmpty()) throw new RuntimeException("No data from Twelve Data");
                Map<String, String> latest = values.get(0);
                MarketData dto = new MarketData();
                dto.setSymbol(symbol);
                dto.setTimestamp(Instant.parse(latest.get("datetime") + "Z"));
                dto.setOpen(Double.parseDouble(latest.get("open")));
                dto.setHigh(Double.parseDouble(latest.get("high")));
                dto.setLow(Double.parseDouble(latest.get("low")));
                dto.setClose(Double.parseDouble(latest.get("close")));
                dto.setVolume(Long.parseLong(latest.get("volume")));
                return dto;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn("TwelveData 429 rate limit hit, retry {}/{} after {}ms", attempt, maxRetries, backoff);
                    Thread.sleep(backoff);
                    backoff *= 2;
                } else {
                    logger.error("TwelveData HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw e;
                }
            } catch (ResourceAccessException e) {
                logger.warn("TwelveData network timeout, retry {}/{} after {}ms", attempt, maxRetries, backoff);
                Thread.sleep(backoff);
                backoff *= 2;
            } catch (Exception e) {
                logger.warn("TwelveData error ({}), retry {}/{} after {}ms: {}", e.getClass().getSimpleName(), attempt, maxRetries, backoff, e.getMessage());
                if (attempt == maxRetries) throw e;
                Thread.sleep(backoff);
                backoff *= 2;
            }
        }
        throw new RuntimeException("TwelveData retries exhausted");
    }

    @Override
    public String getName() { return "TwelveData"; }
} 