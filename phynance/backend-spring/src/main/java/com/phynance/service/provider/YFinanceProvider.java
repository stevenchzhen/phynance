package com.phynance.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phynance.model.MarketDataDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class YFinanceProvider implements FinancialDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(YFinanceProvider.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YFinanceProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // Configure RestTemplate to handle gzip compression
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate.setRequestFactory(factory);
    }

    @Override
    public String getName() {
        return "YFinance";
    }

    @Override
    public MarketDataDto getMarketData(String symbol) throws Exception {
        try {
            // Yahoo Finance API endpoint for real-time data
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=1d", symbol);
            
            // Add headers to mimic a real browser request
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Connection", "keep-alive");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // Add a small delay to avoid rate limiting
            Thread.sleep(1000);
            
            org.springframework.http.ResponseEntity<String> responseEntity = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();
            if (response == null) {
                throw new RuntimeException("No response from Yahoo Finance");
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode chart = root.get("chart");
            JsonNode result = chart.get("result").get(0);
            JsonNode meta = result.get("meta");
            JsonNode timestamp = result.get("timestamp");
            JsonNode indicators = result.get("indicators");
            JsonNode quote = indicators.get("quote").get(0);

            // Extract OHLCV data
            JsonNode open = quote.get("open");
            JsonNode high = quote.get("high");
            JsonNode low = quote.get("low");
            JsonNode close = quote.get("close");
            JsonNode volume = quote.get("volume");

            // Get the latest data point
            int lastIndex = open.size() - 1;
            
            MarketDataDto dto = new MarketDataDto();
            dto.setSymbol(symbol);
            dto.setTimestamp(Instant.now());
            dto.setOpen(open.get(lastIndex).asDouble());
            dto.setHigh(high.get(lastIndex).asDouble());
            dto.setLow(low.get(lastIndex).asDouble());
            dto.setClose(close.get(lastIndex).asDouble());
            dto.setVolume(volume.get(lastIndex).asLong());

            logger.info("Successfully fetched data for {} from Yahoo Finance", symbol);
            return dto;

        } catch (Exception e) {
            logger.error("Error fetching data for {} from Yahoo Finance: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch data from Yahoo Finance: " + e.getMessage());
        }
    }

    public List<MarketDataDto> getHistoricalData(String symbol, String startDate, String endDate) {
        try {
            // Convert dates to Unix timestamps
            long startTimestamp = LocalDateTime.parse(startDate + "T00:00:00").toEpochSecond(ZoneOffset.UTC);
            long endTimestamp = LocalDateTime.parse(endDate + "T23:59:59").toEpochSecond(ZoneOffset.UTC);
            
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d", 
                                     symbol, startTimestamp, endTimestamp);
            
            // Add headers to mimic a real browser request
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Connection", "keep-alive");
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // Add a small delay to avoid rate limiting
            Thread.sleep(1000);
            
            org.springframework.http.ResponseEntity<String> responseEntity = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();
            if (response == null) {
                throw new RuntimeException("No response from Yahoo Finance");
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode chart = root.get("chart");
            JsonNode result = chart.get("result").get(0);
            JsonNode timestamp = result.get("timestamp");
            JsonNode indicators = result.get("indicators");
            JsonNode quote = indicators.get("quote").get(0);

            JsonNode open = quote.get("open");
            JsonNode high = quote.get("high");
            JsonNode low = quote.get("low");
            JsonNode close = quote.get("close");
            JsonNode volume = quote.get("volume");

            List<MarketDataDto> historicalData = new ArrayList<>();
            
            for (int i = 0; i < timestamp.size(); i++) {
                if (open.get(i).isNull() || close.get(i).isNull()) {
                    continue; // Skip null data points
                }
                
                MarketDataDto dto = new MarketDataDto();
                dto.setSymbol(symbol);
                dto.setTimestamp(Instant.ofEpochSecond(timestamp.get(i).asLong()));
                dto.setOpen(open.get(i).asDouble());
                dto.setHigh(high.get(i).asDouble());
                dto.setLow(low.get(i).asDouble());
                dto.setClose(close.get(i).asDouble());
                dto.setVolume(volume.get(i).asLong());
                
                historicalData.add(dto);
            }

            logger.info("Successfully fetched {} historical data points for {} from Yahoo Finance", historicalData.size(), symbol);
            return historicalData;

        } catch (Exception e) {
            logger.error("Error fetching historical data for {} from Yahoo Finance: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch historical data from Yahoo Finance: " + e.getMessage());
        }
    }

    public List<MarketDataDto> getHourlyData(String symbol, String startDate, String endDate) {
        try {
            // Convert dates to Unix timestamps
            long startTimestamp = LocalDateTime.parse(startDate + "T00:00:00").toEpochSecond(ZoneOffset.UTC);
            long endTimestamp = LocalDateTime.parse(endDate + "T23:59:59").toEpochSecond(ZoneOffset.UTC);

            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1h", 
                                     symbol, startTimestamp, endTimestamp);

            // Add headers to mimic a real browser request
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Connection", "keep-alive");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            // Add a small delay to avoid rate limiting
            Thread.sleep(1000);

            org.springframework.http.ResponseEntity<String> responseEntity = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            String response = responseEntity.getBody();
            if (response == null) {
                throw new RuntimeException("No response from Yahoo Finance");
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode chart = root.get("chart");
            JsonNode result = chart.get("result").get(0);
            JsonNode timestamp = result.get("timestamp");
            JsonNode indicators = result.get("indicators");
            JsonNode quote = indicators.get("quote").get(0);

            JsonNode open = quote.get("open");
            JsonNode high = quote.get("high");
            JsonNode low = quote.get("low");
            JsonNode close = quote.get("close");
            JsonNode volume = quote.get("volume");

            List<MarketDataDto> hourlyData = new ArrayList<>();

            for (int i = 0; i < timestamp.size(); i++) {
                if (open.get(i).isNull() || close.get(i).isNull()) {
                    continue; // Skip null data points
                }

                MarketDataDto dto = new MarketDataDto();
                dto.setSymbol(symbol);
                dto.setTimestamp(Instant.ofEpochSecond(timestamp.get(i).asLong()));
                dto.setOpen(open.get(i).asDouble());
                dto.setHigh(high.get(i).asDouble());
                dto.setLow(low.get(i).asDouble());
                dto.setClose(close.get(i).asDouble());
                dto.setVolume(volume.get(i).asLong());

                hourlyData.add(dto);
            }

            // Only return the last 60 data points
            int fromIndex = Math.max(0, hourlyData.size() - 60);
            List<MarketDataDto> last60 = hourlyData.subList(fromIndex, hourlyData.size());

            logger.info("Successfully fetched {} hourly data points for {} from Yahoo Finance", last60.size(), symbol);
            return last60;

        } catch (Exception e) {
            logger.error("Error fetching hourly data for {} from Yahoo Finance: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch hourly data from Yahoo Finance: " + e.getMessage());
        }
    }
} 