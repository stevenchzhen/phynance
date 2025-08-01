package com.phynance.model;

import java.time.Instant;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity for market OHLCV time series data.
 * Partitioning strategy: partition table by month on timestamp (see migration script).
 */
@Entity
@Table(name = "market_data")
public class MarketData implements Serializable {
    @Id
    @Column(nullable = false)
    private String symbol;

    @Id
    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private double open;
    @Column(nullable = false)
    private double high;
    @Column(nullable = false)
    private double low;
    @Column(nullable = false)
    private double close;
    @Column(nullable = false)
    private long volume;

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public double getOpen() { return open; }
    public void setOpen(double open) { this.open = open; }
    public double getHigh() { return high; }
    public void setHigh(double high) { this.high = high; }
    public double getLow() { return low; }
    public void setLow(double low) { this.low = low; }
    public double getClose() { return close; }
    public void setClose(double close) { this.close = close; }
    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketData that = (MarketData) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, timestamp);
    }
} 