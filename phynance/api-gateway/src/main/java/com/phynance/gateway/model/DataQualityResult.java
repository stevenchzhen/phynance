package com.phynance.gateway.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of data quality validation across multiple APIs
 */
public class DataQualityResult {
    
    private String symbol;
    private String dataType;
    private double confidenceScore;
    private DataQualityStatus status;
    private Instant validationTime;
    private Map<String, Object> primaryData;
    private Map<String, Object> validatedData;
    private List<DataAnomaly> anomalies;
    private List<String> warnings;
    private Map<String, Double> apiAgreementScores;
    private boolean isCleaned;
    private String cleaningNotes;
    
    public DataQualityResult(String symbol, String dataType) {
        this.symbol = symbol;
        this.dataType = dataType;
        this.validationTime = Instant.now();
        this.confidenceScore = 1.0;
        this.status = DataQualityStatus.PENDING;
        this.anomalies = new java.util.ArrayList<>();
        this.warnings = new java.util.ArrayList<>();
        this.apiAgreementScores = new java.util.HashMap<>();
        this.isCleaned = false;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public DataQualityStatus getStatus() { return status; }
    public void setStatus(DataQualityStatus status) { this.status = status; }
    
    public Instant getValidationTime() { return validationTime; }
    public void setValidationTime(Instant validationTime) { this.validationTime = validationTime; }
    
    public Map<String, Object> getPrimaryData() { return primaryData; }
    public void setPrimaryData(Map<String, Object> primaryData) { this.primaryData = primaryData; }
    
    public Map<String, Object> getValidatedData() { return validatedData; }
    public void setValidatedData(Map<String, Object> validatedData) { this.validatedData = validatedData; }
    
    public List<DataAnomaly> getAnomalies() { return anomalies; }
    public void setAnomalies(List<DataAnomaly> anomalies) { this.anomalies = anomalies; }
    
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    
    public Map<String, Double> getApiAgreementScores() { return apiAgreementScores; }
    public void setApiAgreementScores(Map<String, Double> apiAgreementScores) { this.apiAgreementScores = apiAgreementScores; }
    
    public boolean isCleaned() { return isCleaned; }
    public void setCleaned(boolean cleaned) { isCleaned = cleaned; }
    
    public String getCleaningNotes() { return cleaningNotes; }
    public void setCleaningNotes(String cleaningNotes) { this.cleaningNotes = cleaningNotes; }
    
    // Business methods
    public void addAnomaly(DataAnomaly anomaly) {
        this.anomalies.add(anomaly);
        updateConfidenceScore();
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public void addApiAgreementScore(String apiName, double score) {
        this.apiAgreementScores.put(apiName, score);
        updateConfidenceScore();
    }
    
    private void updateConfidenceScore() {
        // Reduce confidence based on anomalies and API disagreement
        double anomalyPenalty = anomalies.size() * 0.1;
        double agreementPenalty = 1.0 - apiAgreementScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);
        
        this.confidenceScore = Math.max(0.0, 1.0 - anomalyPenalty - agreementPenalty);
        
        // Update status based on confidence
        if (confidenceScore >= 0.9) {
            this.status = DataQualityStatus.EXCELLENT;
        } else if (confidenceScore >= 0.7) {
            this.status = DataQualityStatus.GOOD;
        } else if (confidenceScore >= 0.5) {
            this.status = DataQualityStatus.FAIR;
        } else {
            this.status = DataQualityStatus.POOR;
        }
    }
    
    public boolean hasAnomalies() {
        return !anomalies.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public boolean isHighQuality() {
        return confidenceScore >= 0.8 && status != DataQualityStatus.POOR;
    }
    
    public enum DataQualityStatus {
        PENDING,
        EXCELLENT,
        GOOD,
        FAIR,
        POOR,
        FAILED
    }
    
    public static class DataAnomaly {
        private String field;
        private String type;
        private String description;
        private double severity;
        private Object expectedValue;
        private Object actualValue;
        
        public DataAnomaly(String field, String type, String description, double severity) {
            this.field = field;
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        
        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public double getSeverity() { return severity; }
        public void setSeverity(double severity) { this.severity = severity; }
        
        public Object getExpectedValue() { return expectedValue; }
        public void setExpectedValue(Object expectedValue) { this.expectedValue = expectedValue; }
        
        public Object getActualValue() { return actualValue; }
        public void setActualValue(Object actualValue) { this.actualValue = actualValue; }
    }
} 