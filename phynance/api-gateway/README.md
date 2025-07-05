# Phynance API Gateway

A high-performance Spring Boot API Gateway service that manages rate limits for multiple financial APIs with intelligent request queuing and smart request distribution.

## Features

### 🚀 Rate Limiting

- **Yahoo Finance**: 2000 requests/hour (1 every 1.8 seconds)
- **Alpha Vantage**: 5 requests/minute (1 every 12 seconds)
- **Twelve Data**: 8 requests/minute (1 every 7.5 seconds)
- **Polygon**: 5 requests/minute (1 every 12 seconds)

### 📊 Request Queue System

- **HIGH Priority**: Physics model calculations
- **MEDIUM Priority**: User dashboard requests
- **LOW Priority**: Background data updates
- **Dead Letter Queue**: Failed requests after max retries

### 🔄 Smart Request Distribution

- Automatic switching between APIs when rate limits hit
- Load balancing across available APIs
- Fallback to cached data when all APIs exhausted
- Circuit breaker pattern for failed APIs

### 📈 Queue Management Features

- Real-time queue status monitoring
- Estimated wait times for requests
- Request cancellation capability
- Batch processing for bulk requests

### 📊 Monitoring & Metrics

- Comprehensive logging
- Metrics collection via Prometheus
- Admin endpoints for monitoring
- Real-time API usage patterns

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Redis (for caching)
- Docker (optional)

### Running with Docker

1. **Start Redis**:

```bash
docker run -d -p 6379:6379 --name redis-cache redis:latest
```

2. **Build and run the API Gateway**:

```bash
cd phynance/api-gateway
mvn clean package
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

The API Gateway will start on port 8081.

## API Endpoints

### Request Submission

#### High Priority Requests (Physics Models)

```bash
POST /api/gateway/request/high?symbol=AAPL&endpoint=chart
```

#### Medium Priority Requests (Dashboard)

```bash
POST /api/gateway/request/medium?symbol=AAPL&endpoint=quote
```

#### Low Priority Requests (Background Updates)

```bash
POST /api/gateway/request/low?symbol=AAPL&endpoint=historical
```

### Monitoring Endpoints

#### Gateway Statistics

```bash
GET /api/gateway/stats
```

#### Estimated Wait Time

```bash
GET /api/gateway/wait-time/HIGH
GET /api/gateway/wait-time/MEDIUM
GET /api/gateway/wait-time/LOW
```

#### Health Check

```bash
GET /api/gateway/health
```

### Admin Endpoints

#### System Overview

```bash
GET /api/admin/overview
```

#### Performance Metrics

```bash
GET /api/admin/metrics
```

#### System Alerts

```bash
GET /api/admin/alerts
```

#### Rate Limiter Details

```bash
GET /api/admin/rate-limiters
```

#### Queue Details

```bash
GET /api/admin/queues
```

## Configuration

### Application Properties

The main configuration is in `src/main/resources/application.yml`:

```yaml
# API Provider Rate Limits
api:
  providers:
    yahoo-finance:
      rate-limit:
        requests-per-hour: 2000
        requests-per-minute: 33
        requests-per-second: 0.56
      circuit-breaker:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        minimum-number-of-calls: 5

# Queue Configuration
queue:
  max-queue-size: 1000
  processing-threads: 4
  dead-letter-queue:
    max-retries: 3
    retry-delay: 30s
```

### Redis Configuration

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
```

## Architecture

### Core Components

1. **RateLimiterService**: Manages rate limits for each API provider using Guava RateLimiter
2. **RequestQueueService**: Handles priority queues with dead letter queue support
3. **ApiGatewayService**: Orchestrates request processing with circuit breaker pattern
4. **Circuit Breaker**: Uses Resilience4j for fault tolerance

### Request Flow

1. **Request Submission**: Client submits request with priority level
2. **Queue Management**: Request is added to appropriate priority queue
3. **Rate Limiting**: System checks rate limits for available providers
4. **Provider Selection**: Intelligent selection based on availability and priority
5. **API Call**: Makes HTTP request to selected provider
6. **Response Handling**: Caches response and returns to client
7. **Error Handling**: Retries with exponential backoff, moves to dead letter queue if needed

### Circuit Breaker States

- **CLOSED**: Normal operation
- **OPEN**: Provider is failing, requests are rejected
- **HALF_OPEN**: Testing if provider has recovered

## Monitoring

### Metrics Available

- Request counts by priority
- Success/failure rates
- Queue sizes
- Rate limiter statistics
- Circuit breaker states
- Response times
- Error rates

### Prometheus Integration

Metrics are exposed at `/actuator/prometheus` for Prometheus scraping.

### Health Checks

- Application health: `/actuator/health`
- Gateway health: `/api/gateway/health`

## Development

### Building

```bash
mvn clean package
```

### Testing

```bash
mvn test
```

### Running Tests

```bash
mvn clean verify
```

## Deployment

### Docker

```bash
docker build -t phynance-api-gateway .
docker run -p 8081:8081 phynance-api-gateway
```

### Kubernetes

See `k8s/` directory for Kubernetes manifests.

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**

   - Ensure Redis is running on localhost:6379
   - Check Redis configuration in application.yml

2. **Rate Limit Exceeded**

   - Check rate limiter configuration
   - Monitor provider usage patterns

3. **Queue Full**

   - Increase max-queue-size in configuration
   - Check processing thread count

4. **Circuit Breaker Open**
   - Check provider health
   - Review failure rate thresholds

### Logs

Enable debug logging:

```yaml
logging:
  level:
    com.phynance.gateway: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
