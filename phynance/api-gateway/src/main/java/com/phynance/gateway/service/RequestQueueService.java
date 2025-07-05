package com.phynance.gateway.service;

import com.phynance.gateway.model.ApiRequest;
import com.phynance.gateway.model.RequestPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing request queues with priority levels and dead letter queue
 */
@Service
public class RequestQueueService {
    
    private static final Logger log = LoggerFactory.getLogger(RequestQueueService.class);
    
    private PriorityBlockingQueue<ApiRequest> highPriorityQueue;
    private PriorityBlockingQueue<ApiRequest> mediumPriorityQueue;
    private PriorityBlockingQueue<ApiRequest> lowPriorityQueue;
    private Queue<ApiRequest> deadLetterQueue;
    
    private ExecutorService processingExecutor;
    private final Map<String, ApiRequest> activeRequests;
    private final AtomicLong totalRequests;
    private final AtomicLong completedRequests;
    private final AtomicLong failedRequests;
    
    @Value("${queue.max-queue-size:1000}")
    private int maxQueueSize;
    
    @Value("${queue.processing-threads:4}")
    private int processingThreads;
    
    @Value("${queue.dead-letter-queue.max-retries:3}")
    private int maxRetries;
    
    public RequestQueueService() {
        this.activeRequests = new ConcurrentHashMap<>();
        this.totalRequests = new AtomicLong(0);
        this.completedRequests = new AtomicLong(0);
        this.failedRequests = new AtomicLong(0);
    }
    
    @PostConstruct
    public void initialize() {
        this.highPriorityQueue = new PriorityBlockingQueue<>(maxQueueSize, 
                Comparator.comparing(ApiRequest::getCreatedAt));
        this.mediumPriorityQueue = new PriorityBlockingQueue<>(maxQueueSize, 
                Comparator.comparing(ApiRequest::getCreatedAt));
        this.lowPriorityQueue = new PriorityBlockingQueue<>(maxQueueSize, 
                Comparator.comparing(ApiRequest::getCreatedAt));
        this.deadLetterQueue = new ConcurrentLinkedQueue<>();
        
        this.processingExecutor = Executors.newFixedThreadPool(processingThreads);
        
        startQueueProcessor();
        log.info("RequestQueueService initialized with maxQueueSize={}, processingThreads={}", 
                maxQueueSize, processingThreads);
    }
    
    /**
     * Add a request to the appropriate priority queue
     */
    public boolean enqueue(ApiRequest request) {
        if (isQueueFull(request.getPriority())) {
            log.warn("Queue is full for priority: {}", request.getPriority());
            return false;
        }
        
        request.markQueued();
        totalRequests.incrementAndGet();
        
        switch (request.getPriority()) {
            case HIGH:
                return highPriorityQueue.offer(request);
            case MEDIUM:
                return mediumPriorityQueue.offer(request);
            case LOW:
                return lowPriorityQueue.offer(request);
            default:
                log.error("Unknown priority: {}", request.getPriority());
                return false;
        }
    }
    
    /**
     * Get the next request to process based on priority
     */
    public ApiRequest dequeue() throws InterruptedException {
        // Try high priority first
        ApiRequest request = highPriorityQueue.poll();
        if (request != null) {
            return request;
        }
        
        // Try medium priority
        request = mediumPriorityQueue.poll();
        if (request != null) {
            return request;
        }
        
        // Try low priority
        request = lowPriorityQueue.poll();
        if (request != null) {
            return request;
        }
        
        // Wait for any request
        request = highPriorityQueue.take();
        if (request != null) {
            return request;
        }
        
        request = mediumPriorityQueue.take();
        if (request != null) {
            return request;
        }
        
        return lowPriorityQueue.take();
    }
    
    /**
     * Process a failed request with retry logic
     */
    public void handleFailedRequest(ApiRequest request, String error) {
        request.markFailed(error);
        request.incrementRetryCount();
        
        if (request.getRetryCount() >= maxRetries) {
            deadLetterQueue.offer(request);
            failedRequests.incrementAndGet();
            log.error("Request {} moved to dead letter queue after {} retries", 
                    request.getId(), request.getRetryCount());
        } else {
            // Re-queue with exponential backoff
            long delay = (long) Math.pow(2, request.getRetryCount()) * 1000; // milliseconds
            scheduleRetry(request, delay);
        }
    }
    
    /**
     * Schedule a retry for a failed request
     */
    private void scheduleRetry(ApiRequest request, long delayMs) {
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    log.info("Retrying request {} (attempt {})", request.getId(), request.getRetryCount());
                    enqueue(request);
                });
    }
    
    /**
     * Cancel a request
     */
    public boolean cancelRequest(String requestId) {
        ApiRequest request = activeRequests.get(requestId);
        if (request != null) {
            request.setStatus(ApiRequest.RequestStatus.CANCELLED);
            activeRequests.remove(requestId);
            return true;
        }
        return false;
    }
    
    /**
     * Get queue statistics
     */
    public Map<String, Object> getQueueStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("highPriorityQueueSize", highPriorityQueue.size());
        stats.put("mediumPriorityQueueSize", mediumPriorityQueue.size());
        stats.put("lowPriorityQueueSize", lowPriorityQueue.size());
        stats.put("deadLetterQueueSize", deadLetterQueue.size());
        stats.put("activeRequests", activeRequests.size());
        stats.put("totalRequests", totalRequests.get());
        stats.put("completedRequests", completedRequests.get());
        stats.put("failedRequests", failedRequests.get());
        stats.put("successRate", calculateSuccessRate());
        return stats;
    }
    
    /**
     * Get estimated wait time for a priority level
     */
    public long getEstimatedWaitTime(RequestPriority priority) {
        int queueSize = getQueueSize(priority);
        double processingRate = 1.0; // requests per second per thread
        return (long) (queueSize / (processingRate * processingThreads) * 1000); // milliseconds
    }
    
    /**
     * Check if queue is full for a priority level
     */
    private boolean isQueueFull(RequestPriority priority) {
        return getQueueSize(priority) >= maxQueueSize;
    }
    
    /**
     * Get queue size for a priority level
     */
    private int getQueueSize(RequestPriority priority) {
        switch (priority) {
            case HIGH:
                return highPriorityQueue.size();
            case MEDIUM:
                return mediumPriorityQueue.size();
            case LOW:
                return lowPriorityQueue.size();
            default:
                return 0;
        }
    }
    
    /**
     * Calculate success rate
     */
    private double calculateSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 0.0;
        return (double) completedRequests.get() / total * 100.0;
    }
    
    /**
     * Start the queue processor
     */
    private void startQueueProcessor() {
        for (int i = 0; i < processingThreads; i++) {
            processingExecutor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        ApiRequest request = dequeue();
                        if (request != null) {
                            processRequest(request);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Error processing request", e);
                    }
                }
            });
        }
    }
    
    /**
     * Process a request (to be implemented by subclasses or injected services)
     */
    private void processRequest(ApiRequest request) {
        // This will be implemented by the API processing service
        log.debug("Processing request: {}", request.getId());
    }
    
    /**
     * Shutdown the queue service
     */
    public void shutdown() {
        processingExecutor.shutdown();
        try {
            if (!processingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 