package com.phynance.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * API Gateway Application for Phynance
 * 
 * Features:
 * - Rate limiting for multiple financial APIs
 * - Intelligent request queuing with priority levels
 * - Circuit breaker pattern for fault tolerance
 * - Real-time monitoring and metrics
 * - Smart request distribution and load balancing
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
} 