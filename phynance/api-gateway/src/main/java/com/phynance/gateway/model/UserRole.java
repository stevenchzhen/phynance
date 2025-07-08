package com.phynance.gateway.model;

public enum UserRole {
    ADMIN,      // Full system access, user management, API configuration
    ANALYST,    // Advanced physics models, custom parameters, historical data
    TRADER,     // Real-time data, basic models, portfolio tracking
    VIEWER      // Read-only access, limited historical data
} 