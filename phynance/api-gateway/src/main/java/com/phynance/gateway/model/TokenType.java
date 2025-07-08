package com.phynance.gateway.model;

public enum TokenType {
    ACCESS,   // 15-minute expiry for API calls
    REFRESH   // 7-day expiry for token renewal
} 