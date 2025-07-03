package com.phynance.model;

public class ComprehensiveAnalysisResponse {
    private String symbol;
    private HarmonicOscillatorAnalysisResponse harmonicOscillator;
    private ThermodynamicsAnalysisResponse marketTemperature;
    private WavePhysicsAnalysisResponse waveInterference;
    private EnsembleResult ensemble;
    private ModelPerformance modelPerformance;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public HarmonicOscillatorAnalysisResponse getHarmonicOscillator() { return harmonicOscillator; }
    public void setHarmonicOscillator(HarmonicOscillatorAnalysisResponse harmonicOscillator) { this.harmonicOscillator = harmonicOscillator; }
    public ThermodynamicsAnalysisResponse getMarketTemperature() { return marketTemperature; }
    public void setMarketTemperature(ThermodynamicsAnalysisResponse marketTemperature) { this.marketTemperature = marketTemperature; }
    public WavePhysicsAnalysisResponse getWaveInterference() { return waveInterference; }
    public void setWaveInterference(WavePhysicsAnalysisResponse waveInterference) { this.waveInterference = waveInterference; }
    public EnsembleResult getEnsemble() { return ensemble; }
    public void setEnsemble(EnsembleResult ensemble) { this.ensemble = ensemble; }
    public ModelPerformance getModelPerformance() { return modelPerformance; }
    public void setModelPerformance(ModelPerformance modelPerformance) { this.modelPerformance = modelPerformance; }

    public static class EnsembleResult {
        private String consensusSignal;
        private double confidenceScore;
        private String modelAgreement;
        private double predictedPrice;
        private double[] priceRange;
        private String timeframe;
        private String reasoning;

        public String getConsensusSignal() { return consensusSignal; }
        public void setConsensusSignal(String consensusSignal) { this.consensusSignal = consensusSignal; }
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getModelAgreement() { return modelAgreement; }
        public void setModelAgreement(String modelAgreement) { this.modelAgreement = modelAgreement; }
        public double getPredictedPrice() { return predictedPrice; }
        public void setPredictedPrice(double predictedPrice) { this.predictedPrice = predictedPrice; }
        public double[] getPriceRange() { return priceRange; }
        public void setPriceRange(double[] priceRange) { this.priceRange = priceRange; }
        public String getTimeframe() { return timeframe; }
        public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    }

    public static class ModelPerformance {
        private double harmonicAccuracy;
        private double temperatureAccuracy;
        private double waveAccuracy;
        private double ensembleAccuracy;

        public double getHarmonicAccuracy() { return harmonicAccuracy; }
        public void setHarmonicAccuracy(double harmonicAccuracy) { this.harmonicAccuracy = harmonicAccuracy; }
        public double getTemperatureAccuracy() { return temperatureAccuracy; }
        public void setTemperatureAccuracy(double temperatureAccuracy) { this.temperatureAccuracy = temperatureAccuracy; }
        public double getWaveAccuracy() { return waveAccuracy; }
        public void setWaveAccuracy(double waveAccuracy) { this.waveAccuracy = waveAccuracy; }
        public double getEnsembleAccuracy() { return ensembleAccuracy; }
        public void setEnsembleAccuracy(double ensembleAccuracy) { this.ensembleAccuracy = ensembleAccuracy; }
    }
} 