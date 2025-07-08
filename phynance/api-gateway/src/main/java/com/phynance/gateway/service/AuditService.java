package com.phynance.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        
        // In a production environment, you would also store this in a database
        // or send to a centralized logging system like ELK stack
    }
    
    public void logApiAccess(String username, String endpoint, String method, String ipAddress, int statusCode) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[API_ACCESS] %s | User: %s | %s %s | IP: %s | Status: %d",
                timestamp, username, method, endpoint, ipAddress, statusCode);
        
        logger.info(logMessage);
    }
    
    public void logError(String username, String error, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[ERROR] %s | User: %s | Error: %s | IP: %s | User-Agent: %s",
                timestamp, username, error, ipAddress != null ? ipAddress : "N/A", 
                userAgent != null ? userAgent : "N/A");
        
        logger.error(logMessage);
    }
} 