package com.phynance.model;

import java.util.List;

/**
 * Request for financial harmonic oscillator analysis.
 */
public class HarmonicOscillatorRequest {
    /** List of historical OHLCV data points. */
    private List<Ohlcv> ohlcvData;
    /** Number of days to predict (default: 5). */
    private int predictionDays = 5;

    // Getters and setters
    public List<Ohlcv> getOhlcvData() { return ohlcvData; }
    public void setOhlcvData(List<Ohlcv> ohlcvData) { this.ohlcvData = ohlcvData; }
    public int getPredictionDays() { return predictionDays; }
    public void setPredictionDays(int predictionDays) { this.predictionDays = predictionDays; }

    /**
     * OHLCV data structure.
     */
    public static class Ohlcv {
        private String date;
        private double open;
        private double high;
        private double low;
        private double close;
        private long volume;
        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
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
    }
} 