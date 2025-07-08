package com.phynance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void logSecurityEvent(String username, String event, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[AUDIT] %s | User: %s | Event: %s | IP: %s | User-Agent: %s",
                timestamp, username, event, ipAddress != null ? ipAddress : "N/A", 
                userAgent != null ? userAgent : "N/A");
        
        logger.info(logMessage);
    }
    
    public void logApiAccess(String username, String endpoint, String method, String ipAddress, int statusCode) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[API_ACCESS] %s | User: %s | %s %s | IP: %s | Status: %d",
                timestamp, username, method, endpoint, ipAddress, statusCode);
        
        logger.info(logMessage);
    }
    
    public void logMethodAccess(String username, String methodName, String className, String result) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[METHOD_ACCESS] %s | User: %s | Method: %s.%s | Result: %s",
                timestamp, username, className, methodName, result);
        
        logger.info(logMessage);
    }
    
    public void logAccessDenied(String username, String methodName, String className, String reason) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[ACCESS_DENIED] %s | User: %s | Method: %s.%s | Reason: %s",
                timestamp, username, className, methodName, reason);
        
        logger.warn(logMessage);
    }
    
    public void logRateLimitExceeded(String username, String endpoint, String limit) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[RATE_LIMIT] %s | User: %s | Endpoint: %s | Limit: %s",
                timestamp, username, endpoint, limit);
        
        logger.warn(logMessage);
    }
    
    public void logDataAccess(String username, String dataType, String symbol, int dataPoints) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[DATA_ACCESS] %s | User: %s | Type: %s | Symbol: %s | Points: %d",
                timestamp, username, dataType, symbol, dataPoints);
        
        logger.info(logMessage);
    }
    
    public void logError(String username, String error, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[ERROR] %s | User: %s | Error: %s | IP: %s | User-Agent: %s",
                timestamp, username, error, ipAddress != null ? ipAddress : "N/A", 
                userAgent != null ? userAgent : "N/A");
        
        logger.error(logMessage);
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
} 