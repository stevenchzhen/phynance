# Method-Level Security Implementation for Physics Model Access

## Overview

This document outlines the comprehensive method-level security implementation for the Phynance financial platform, providing role-based access control for physics model access with detailed permissions, rate limiting, and audit logging.

## Role-Based Access Control

### 1. VIEWER Role

- **API Rate Limit**: 10 requests per hour
- **Calculation Limit**: 5 calculations per hour
- **Data Access**: Last 30 days only
- **Symbol Limit**: 1 symbol at a time
- **Features**:
  - Basic harmonic oscillator with default parameters only
  - Pre-calculated models
  - Market data summary (30 days)
  - Limited symbol list (AAPL, SPY, TSLA, MSFT, GOOGL)

### 2. TRADER Role

- **API Rate Limit**: 100 requests per hour
- **Calculation Limit**: 10 calculations per hour
- **Data Access**: Last 2 years
- **Symbol Limit**: 20 symbols simultaneously
- **Features**:
  - Harmonic oscillator with limited parameter ranges
  - Real-time market data streaming
  - Extended historical data access
  - Custom prediction days (1-30)

### 3. ANALYST Role

- **API Rate Limit**: 1000 requests per hour
- **Calculation Limit**: Unlimited
- **Data Access**: Full historical data
- **Symbol Limit**: Unlimited
- **Features**:
  - Full model access with custom parameters
  - Advanced physics models (thermodynamic, wave interference)
  - Unlimited calculations
  - Bulk data export capabilities

### 4. ADMIN Role

- **API Rate Limit**: Unlimited
- **Calculation Limit**: Unlimited
- **Data Access**: All data plus system metrics
- **Symbol Limit**: Unlimited
- **Features**:
  - All ANALYST features
  - Model configuration and performance monitoring
  - User activity and access logs
  - System performance metrics
  - Bulk data export with full access

## Implemented Security Features

### 1. Method-Level Security Annotations

#### Harmonic Oscillator Controller

```java
@PostMapping("/harmonic-oscillator")
@PreAuthorize("hasAnyRole('VIEWER','TRADER','ANALYST','ADMIN')")
@PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
```

#### Thermodynamics Controller (Advanced Model)

```java
@PostMapping("/thermodynamics")
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
@PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
```

#### Wave Physics Controller (Advanced Model)

```java
@PostMapping("/wave-physics")
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
@PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
```

#### Market Data Controller (Real-time Data)

```java
@GetMapping("/api/v1/market-data")
@PreAuthorize("hasAnyRole('TRADER','ANALYST','ADMIN')")
@PostAuthorize("returnObject.statusCode.value() == 200 or returnObject.statusCode.value() == 400 or returnObject.statusCode.value() == 403")
```

#### Financial Data Service

```java
@PreAuthorize("hasAnyRole('TRADER','ANALYST','ADMIN')")
public MarketData getMarketData(String symbol)

@PreAuthorize("hasAnyRole('VIEWER','TRADER','ANALYST','ADMIN')")
@PostAuthorize("returnObject.size() <= (hasRole('ADMIN') ? 10000 : (hasRole('ANALYST') ? 5000 : (hasRole('TRADER') ? 730 : 30)))")
public List<MarketData> getHistoricalData(String symbol, String startDate, String endDate)
```

### 2. Rate Limiting Service

The `RateLimitService` provides comprehensive rate limiting with role-based limits:

- **API Rate Limits**: Per-endpoint rate limiting based on user role
- **Calculation Limits**: Model-specific calculation limits per hour
- **Data Access Limits**: Role-based data point limits
- **Symbol Limits**: Maximum number of symbols per request

### 3. Audit Logging

The `AuditService` provides comprehensive logging for:

- **Security Events**: Login attempts, access denials, role violations
- **API Access**: All API calls with user, endpoint, method, IP, and status
- **Method Access**: Method-level access logging with results
- **Data Access**: Historical data access with symbol and data point counts
- **Rate Limit Violations**: Detailed logging of rate limit exceedances
- **Errors**: Comprehensive error logging with context

### 4. Data Access Restrictions

#### Role-Based Data Limits

- **VIEWER**: 30 days of historical data
- **TRADER**: 2 years of historical data (730 days)
- **ANALYST**: 5,000 data points maximum
- **ADMIN**: 10,000 data points maximum

#### Symbol Access Limits

- **VIEWER**: 1 symbol at a time
- **TRADER**: 20 symbols simultaneously
- **ANALYST/ADMIN**: Unlimited symbols

### 5. Feature Gating

#### Advanced Physics Models

- **Thermodynamics Analysis**: Requires ANALYST+ role
- **Wave Physics Analysis**: Requires ANALYST+ role
- **Real-time Data Streaming**: Requires TRADER+ role

#### Administrative Features

- **Model Performance Analytics**: Requires ADMIN role
- **Bulk Data Export**: Requires ANALYST+ role
- **System Configuration**: Requires ADMIN role

## Controller Implementations

### 1. HarmonicOscillatorAnalysisController

- Role-based parameter restrictions
- Date range validation (30 days for VIEWER, 2 years for TRADER)
- Calculation limit enforcement
- Comprehensive audit logging

### 2. ThermodynamicsAnalysisController

- ANALYST+ role requirement
- Advanced model access control
- Rate limiting and audit logging

### 3. WavePhysicsAnalysisController

- ANALYST+ role requirement
- Advanced physics model access
- Comprehensive security validation

### 4. MarketDataController

- TRADER+ role requirement for real-time data
- Rate limiting and access control
- Data access logging

### 5. AdminController

- ADMIN role requirement for all endpoints
- System performance monitoring
- Model configuration management
- User activity tracking
- Bulk data export capabilities

### 6. ViewerController

- VIEWER role-specific endpoints
- Basic model access with default parameters
- Limited data access (30 days)
- Usage statistics and limits

## Security Configuration

### Spring Security Configuration

- Method-level security enabled with `@EnableMethodSecurity`
- Role-based endpoint access control
- Comprehensive security headers
- CORS configuration
- CSRF protection disabled (stateless JWT)

### Security Headers

- Frame options: DENY
- Content type options enabled
- HSTS with max age
- XSS protection
- Referrer policy: STRICT_ORIGIN_WHEN_CROSS_ORIGIN
- Permissions policy for geolocation, microphone, camera

## Example Usage

### VIEWER Access Example

```bash
# Basic harmonic oscillator with default parameters
GET /api/v1/viewer/harmonic-oscillator/AAPL

# Market summary (30 days)
GET /api/v1/viewer/market-summary/AAPL

# Usage statistics
GET /api/v1/viewer/usage-stats
```

### TRADER Access Example

```bash
# Real-time market data
GET /api/v1/market-data?symbol=AAPL

# Harmonic oscillator with custom parameters
POST /api/v1/analysis/harmonic-oscillator
{
  "symbol": "AAPL",
  "startDate": "2023-01-01",
  "endDate": "2024-01-01",
  "predictionDays": 10
}
```

### ANALYST Access Example

```bash
# Advanced thermodynamics analysis
POST /api/v1/analysis/thermodynamics
{
  "symbol": "AAPL",
  "startDate": "2020-01-01",
  "endDate": "2024-01-01"
}

# Wave physics analysis
POST /api/v1/analysis/wave-physics
{
  "symbol": "AAPL",
  "startDate": "2020-01-01",
  "endDate": "2024-01-01",
  "predictionWeeks": 2
}
```

### ADMIN Access Example

```bash
# System performance metrics
GET /api/v1/admin/performance

# Model analytics
GET /api/v1/admin/model-analytics

# User activity logs
GET /api/v1/admin/user-activity

# Bulk data export
POST /api/v1/admin/export-data
{
  "dataType": "historical",
  "symbol": "AAPL"
}
```

## Audit Log Examples

### Security Event Log

```
[AUDIT] 2024-01-15 14:30:25 | User: analyst1 | Event: THERMODYNAMICS_ANALYSIS_ATTEMPT | IP: 192.168.1.100 | User-Agent: Mozilla/5.0...
```

### Method Access Log

```
[METHOD_ACCESS] 2024-01-15 14:30:26 | User: analyst1 | Method: ThermodynamicsAnalysisController.analyze | Result: SUCCESS
```

### Rate Limit Violation Log

```
[RATE_LIMIT] 2024-01-15 14:35:10 | User: viewer1 | Endpoint: /api/v1/viewer/harmonic-oscillator | Limit: ROLE_VIEWER limit: 10/hour
```

### Data Access Log

```
[DATA_ACCESS] 2024-01-15 14:30:25 | User: analyst1 | Type: historical | Symbol: AAPL | Points: 1250
```

## Compliance and Security Benefits

1. **Role-Based Access Control**: Granular permissions based on user roles
2. **Rate Limiting**: Prevents abuse and ensures fair resource allocation
3. **Audit Logging**: Comprehensive tracking for compliance and security monitoring
4. **Data Access Controls**: Role-based data limits and symbol restrictions
5. **Feature Gating**: Advanced features restricted to appropriate roles
6. **Method-Level Security**: Fine-grained control over method access
7. **Input Validation**: Comprehensive validation with role-based restrictions
8. **Error Handling**: Secure error responses without information leakage

## Future Enhancements

1. **Two-Factor Authentication**: Implementation of 2FA for enhanced security
2. **IP Whitelisting**: IP-based access controls for admin functions
3. **Session Management**: Advanced session tracking and management
4. **Device Fingerprinting**: Device-based security controls
5. **Real-time Monitoring**: Live security monitoring and alerting
6. **Advanced Analytics**: Machine learning-based security analytics
