package com.phynance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "financial.apis")
public class ApiProviderConfig {
    private AlphaVantageConfig alphaVantage;
    private TwelveDataConfig twelveData;

    public AlphaVantageConfig getAlphaVantage() { return alphaVantage; }
    public void setAlphaVantage(AlphaVantageConfig alphaVantage) { this.alphaVantage = alphaVantage; }
    public TwelveDataConfig getTwelveData() { return twelveData; }
    public void setTwelveData(TwelveDataConfig twelveData) { this.twelveData = twelveData; }

    public static class AlphaVantageConfig {
        private String apiKey;
        private String url;
        private double rateLimitPerSecond;
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public double getRateLimitPerSecond() { return rateLimitPerSecond; }
        public void setRateLimitPerSecond(double rateLimitPerSecond) { this.rateLimitPerSecond = rateLimitPerSecond; }
    }
    public static class TwelveDataConfig {
        private String apiKey;
        private String url;
        private double rateLimitPerSecond;
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public double getRateLimitPerSecond() { return rateLimitPerSecond; }
        public void setRateLimitPerSecond(double rateLimitPerSecond) { this.rateLimitPerSecond = rateLimitPerSecond; }
    }
} 