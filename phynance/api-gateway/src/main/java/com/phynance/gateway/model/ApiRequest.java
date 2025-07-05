package com.phynance.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an API request with priority and tracking information
 */
public class ApiRequest {
    
    private String id;
    private String symbol;
    private String endpoint;
    private RequestPriority priority;
    private String provider;
    private Instant createdAt;
    private Instant queuedAt;
    private Instant processedAt;
    private RequestStatus status;
    private int retryCount;
    private String errorMessage;
    private Object requestData;
    private Object responseData;
    
    @JsonIgnore
    private long estimatedWaitTime;
    
    // Default constructor
    public ApiRequest() {}
    
    // Builder constructor
    private ApiRequest(Builder builder) {
        this.id = builder.id;
        this.symbol = builder.symbol;
        this.endpoint = builder.endpoint;
        this.priority = builder.priority;
        this.provider = builder.provider;
        this.createdAt = builder.createdAt;
        this.queuedAt = builder.queuedAt;
        this.processedAt = builder.processedAt;
        this.status = builder.status;
        this.retryCount = builder.retryCount;
        this.errorMessage = builder.errorMessage;
        this.requestData = builder.requestData;
        this.responseData = builder.responseData;
        this.estimatedWaitTime = builder.estimatedWaitTime;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public RequestPriority getPriority() { return priority; }
    public void setPriority(RequestPriority priority) { this.priority = priority; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getQueuedAt() { return queuedAt; }
    public void setQueuedAt(Instant queuedAt) { this.queuedAt = queuedAt; }
    
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Object getRequestData() { return requestData; }
    public void setRequestData(Object requestData) { this.requestData = requestData; }
    
    public Object getResponseData() { return responseData; }
    public void setResponseData(Object responseData) { this.responseData = responseData; }
    
    public long getEstimatedWaitTime() { return estimatedWaitTime; }
    public void setEstimatedWaitTime(long estimatedWaitTime) { this.estimatedWaitTime = estimatedWaitTime; }
    
    // Builder class
    public static class Builder {
        private String id;
        private String symbol;
        private String endpoint;
        private RequestPriority priority;
        private String provider;
        private Instant createdAt;
        private Instant queuedAt;
        private Instant processedAt;
        private RequestStatus status;
        private int retryCount;
        private String errorMessage;
        private Object requestData;
        private Object responseData;
        private long estimatedWaitTime;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder symbol(String symbol) { this.symbol = symbol; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder priority(RequestPriority priority) { this.priority = priority; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder queuedAt(Instant queuedAt) { this.queuedAt = queuedAt; return this; }
        public Builder processedAt(Instant processedAt) { this.processedAt = processedAt; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder requestData(Object requestData) { this.requestData = requestData; return this; }
        public Builder responseData(Object responseData) { this.responseData = responseData; return this; }
        public Builder estimatedWaitTime(long estimatedWaitTime) { this.estimatedWaitTime = estimatedWaitTime; return this; }
        
        public ApiRequest build() {
            return new ApiRequest(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ApiRequest create(String symbol, String endpoint, RequestPriority priority) {
        return ApiRequest.builder()
                .id(UUID.randomUUID().toString())
                .symbol(symbol)
                .endpoint(endpoint)
                .priority(priority)
                .createdAt(Instant.now())
                .status(RequestStatus.PENDING)
                .retryCount(0)
                .build();
    }
    
    public void markQueued() {
        this.queuedAt = Instant.now();
        this.status = RequestStatus.QUEUED;
    }
    
    public void markProcessing() {
        this.status = RequestStatus.PROCESSING;
    }
    
    public void markCompleted(Object responseData) {
        this.processedAt = Instant.now();
        this.status = RequestStatus.COMPLETED;
        this.responseData = responseData;
    }
    
    public void markFailed(String errorMessage) {
        this.status = RequestStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public enum RequestStatus {
        PENDING,
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
} 