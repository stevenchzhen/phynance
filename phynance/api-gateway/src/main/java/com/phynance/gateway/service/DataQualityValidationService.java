package com.phynance.gateway.service;

import com.phynance.gateway.model.DataQualityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for validating data quality across multiple API providers
 */
@Service
public class DataQualityValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(DataQualityValidationService.class);
    
    private final Map<String, Map<String, Object>> dataCache;
    private final Map<String, Double> priceThresholds;
    private final Map<String, Double> volumeThresholds;
    
    public DataQualityValidationService() {
        this.dataCache = new ConcurrentHashMap<>();
        this.priceThresholds = initializePriceThresholds();
        this.volumeThresholds = initializeVolumeThresholds();
    }
    
    /**
     * Validate data quality across multiple API responses
     */
    public DataQualityResult validateDataQuality(String symbol, String dataType, 
                                                Map<String, Object> primaryData,
                                                Map<String, Object> secondaryData) {
        DataQualityResult result = new DataQualityResult(symbol, dataType);
        result.setPrimaryData(primaryData);
        
        if (primaryData == null || primaryData.isEmpty()) {
            result.setStatus(DataQualityResult.DataQualityStatus.FAILED);
            result.addWarning("No primary data available");
            return result;
        }
        
        // Perform data quality checks
        validatePriceData(result, primaryData, secondaryData);
        validateVolumeData(result, primaryData, secondaryData);
        validateDataCompleteness(result, primaryData);
        validateDataConsistency(result, primaryData, secondaryData);
        validateAnomalies(result, primaryData);
        
        // Calculate API agreement scores
        calculateApiAgreementScores(result, primaryData, secondaryData);
        
        // Clean data if necessary
        if (result.hasAnomalies() || result.getConfidenceScore() < 0.8) {
            cleanData(result);
        }
        
        log.info("Data quality validation completed for {}: confidence={}, status={}", 
                symbol, result.getConfidenceScore(), result.getStatus());
        
        return result;
    }
    
    /**
     * Validate price data for anomalies and consistency
     */
    private void validatePriceData(DataQualityResult result, Map<String, Object> primaryData, 
                                 Map<String, Object> secondaryData) {
        try {
            Double primaryPrice = extractPrice(primaryData);
            if (primaryPrice == null) {
                result.addAnomaly(new DataQualityResult.DataAnomaly(
                        "price", "MISSING", "Price data is missing", 0.8));
                return;
            }
            
            // Check for price spikes
            Double threshold = priceThresholds.get(result.getSymbol());
            if (threshold != null && Math.abs(primaryPrice) > threshold) {
                result.addAnomaly(new DataQualityResult.DataAnomaly(
                        "price", "SPIKE", "Price exceeds threshold", 0.6));
            }
            
            // Cross-validate with secondary data
            if (secondaryData != null) {
                Double secondaryPrice = extractPrice(secondaryData);
                if (secondaryPrice != null) {
                    double priceDifference = Math.abs(primaryPrice - secondaryPrice);
                    double percentageDifference = (priceDifference / primaryPrice) * 100;
                    
                    if (percentageDifference > 5.0) { // More than 5% difference
                        result.addAnomaly(new DataQualityResult.DataAnomaly(
                                "price", "DISCREPANCY", 
                                String.format("Price difference: %.2f%%", percentageDifference), 
                                0.7));
                    }
                    
                    // Add agreement score
                    double agreementScore = Math.max(0, 1.0 - (percentageDifference / 100.0));
                    result.addApiAgreementScore("price_agreement", agreementScore);
                }
            }
            
        } catch (Exception e) {
            result.addWarning("Error validating price data: " + e.getMessage());
        }
    }
    
    /**
     * Validate volume data for anomalies
     */
    private void validateVolumeData(DataQualityResult result, Map<String, Object> primaryData,
                                  Map<String, Object> secondaryData) {
        try {
            Long primaryVolume = extractVolume(primaryData);
            if (primaryVolume == null) {
                result.addAnomaly(new DataQualityResult.DataAnomaly(
                        "volume", "MISSING", "Volume data is missing", 0.5));
                return;
            }
            
            // Check for volume anomalies
            Double threshold = volumeThresholds.get(result.getSymbol());
            if (threshold != null && primaryVolume > threshold) {
                result.addAnomaly(new DataQualityResult.DataAnomaly(
                        "volume", "ANOMALY", "Volume exceeds threshold", 0.4));
            }
            
            // Cross-validate with secondary data
            if (secondaryData != null) {
                Long secondaryVolume = extractVolume(secondaryData);
                if (secondaryVolume != null) {
                    double volumeDifference = Math.abs(primaryVolume - secondaryVolume);
                    double percentageDifference = (volumeDifference / (double) primaryVolume) * 100;
                    
                    if (percentageDifference > 20.0) { // More than 20% difference
                        result.addAnomaly(new DataQualityResult.DataAnomaly(
                                "volume", "DISCREPANCY",
                                String.format("Volume difference: %.2f%%", percentageDifference),
                                0.5));
                    }
                    
                    // Add agreement score
                    double agreementScore = Math.max(0, 1.0 - (percentageDifference / 100.0));
                    result.addApiAgreementScore("volume_agreement", agreementScore);
                }
            }
            
        } catch (Exception e) {
            result.addWarning("Error validating volume data: " + e.getMessage());
        }
    }
    
    /**
     * Validate data completeness
     */
    private void validateDataCompleteness(DataQualityResult result, Map<String, Object> data) {
        List<String> requiredFields = Arrays.asList("price", "volume", "timestamp");
        List<String> missingFields = new ArrayList<>();
        
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null) {
                missingFields.add(field);
            }
        }
        
        if (!missingFields.isEmpty()) {
            result.addAnomaly(new DataQualityResult.DataAnomaly(
                    "completeness", "MISSING_FIELDS",
                    "Missing required fields: " + String.join(", ", missingFields),
                    0.6));
        }
    }
    
    /**
     * Validate data consistency
     */
    private void validateDataConsistency(DataQualityResult result, Map<String, Object> primaryData,
                                       Map<String, Object> secondaryData) {
        if (secondaryData == null) {
            return;
        }
        
        Set<String> commonFields = new HashSet<>(primaryData.keySet());
        commonFields.retainAll(secondaryData.keySet());
        
        int consistentFields = 0;
        int totalFields = commonFields.size();
        
        for (String field : commonFields) {
            Object primaryValue = primaryData.get(field);
            Object secondaryValue = secondaryData.get(field);
            
            if (Objects.equals(primaryValue, secondaryValue)) {
                consistentFields++;
            }
        }
        
        if (totalFields > 0) {
            double consistencyScore = (double) consistentFields / totalFields;
            result.addApiAgreementScore("field_consistency", consistencyScore);
            
            if (consistencyScore < 0.7) {
                result.addWarning("Low data consistency across APIs: " + 
                        String.format("%.1f%%", consistencyScore * 100));
            }
        }
    }
    
    /**
     * Validate for statistical anomalies
     */
    private void validateAnomalies(DataQualityResult result, Map<String, Object> data) {
        // Check for negative prices
        Double price = extractPrice(data);
        if (price != null && price < 0) {
            result.addAnomaly(new DataQualityResult.DataAnomaly(
                    "price", "NEGATIVE", "Negative price detected", 0.9));
        }
        
        // Check for negative volumes
        Long volume = extractVolume(data);
        if (volume != null && volume < 0) {
            result.addAnomaly(new DataQualityResult.DataAnomaly(
                    "volume", "NEGATIVE", "Negative volume detected", 0.9));
        }
        
        // Check for extreme values
        if (price != null && price > 10000) {
            result.addAnomaly(new DataQualityResult.DataAnomaly(
                    "price", "EXTREME", "Extremely high price detected", 0.7));
        }
        
        if (volume != null && volume > 1000000000L) {
            result.addAnomaly(new DataQualityResult.DataAnomaly(
                    "volume", "EXTREME", "Extremely high volume detected", 0.6));
        }
    }
    
    /**
     * Calculate API agreement scores
     */
    private void calculateApiAgreementScores(DataQualityResult result, Map<String, Object> primaryData,
                                           Map<String, Object> secondaryData) {
        if (secondaryData == null) {
            result.addApiAgreementScore("overall_agreement", 1.0);
            return;
        }
        
        // Calculate overall agreement based on all available scores
        double overallAgreement = result.getApiAgreementScores().values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);
        
        result.addApiAgreementScore("overall_agreement", overallAgreement);
    }
    
    /**
     * Clean and normalize data
     */
    private void cleanData(DataQualityResult result) {
        Map<String, Object> cleanedData = new HashMap<>(result.getPrimaryData());
        
        // Remove or fix anomalies
        for (DataQualityResult.DataAnomaly anomaly : result.getAnomalies()) {
            if (anomaly.getType().equals("NEGATIVE")) {
                // Fix negative values
                if ("price".equals(anomaly.getField())) {
                    Double price = extractPrice(cleanedData);
                    if (price != null && price < 0) {
                        cleanedData.put("price", Math.abs(price));
                    }
                } else if ("volume".equals(anomaly.getField())) {
                    Long volume = extractVolume(cleanedData);
                    if (volume != null && volume < 0) {
                        cleanedData.put("volume", Math.abs(volume));
                    }
                }
            }
        }
        
        result.setValidatedData(cleanedData);
        result.setCleaned(true);
        result.setCleaningNotes("Data cleaned to remove anomalies and normalize values");
        
        log.info("Data cleaned for {}: {} anomalies fixed", result.getSymbol(), result.getAnomalies().size());
    }
    
    /**
     * Extract price from data map
     */
    private Double extractPrice(Map<String, Object> data) {
        if (data == null) return null;
        
        // Try different possible price field names
        String[] priceFields = {"price", "close", "last", "current_price", "value"};
        
        for (String field : priceFields) {
            Object value = data.get(field);
            if (value != null) {
                try {
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    } else {
                        return Double.parseDouble(value.toString());
                    }
                } catch (NumberFormatException e) {
                    // Continue to next field
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract volume from data map
     */
    private Long extractVolume(Map<String, Object> data) {
        if (data == null) return null;
        
        // Try different possible volume field names
        String[] volumeFields = {"volume", "vol", "trading_volume", "shares_traded"};
        
        for (String field : volumeFields) {
            Object value = data.get(field);
            if (value != null) {
                try {
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    } else {
                        return Long.parseLong(value.toString());
                    }
                } catch (NumberFormatException e) {
                    // Continue to next field
                }
            }
        }
        
        return null;
    }
    
    /**
     * Initialize price thresholds for different symbols
     */
    private Map<String, Double> initializePriceThresholds() {
        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put("AAPL", 500.0);
        thresholds.put("GOOGL", 3000.0);
        thresholds.put("TSLA", 1000.0);
        thresholds.put("AMZN", 2000.0);
        thresholds.put("MSFT", 500.0);
        return thresholds;
    }
    
    /**
     * Initialize volume thresholds for different symbols
     */
    private Map<String, Double> initializeVolumeThresholds() {
        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put("AAPL", 100000000.0);
        thresholds.put("GOOGL", 50000000.0);
        thresholds.put("TSLA", 200000000.0);
        thresholds.put("AMZN", 80000000.0);
        thresholds.put("MSFT", 60000000.0);
        return thresholds;
    }
} 