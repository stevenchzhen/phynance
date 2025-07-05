package com.phynance.gateway.model;

/**
 * Request priority levels for the queue system
 */
public enum RequestPriority {
    HIGH(1, "Physics model calculations"),
    MEDIUM(2, "User dashboard requests"),
    LOW(3, "Background data updates");
    
    private final int value;
    private final String description;
    
    RequestPriority(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static RequestPriority fromValue(int value) {
        for (RequestPriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }
} 