// Mock data for Loveable frontend development
// Use this data while designing UI components before connecting to real backend

export const MOCK_API_RESPONSES = {
  // Health check response
  health: {
    service: "Backend-Spring",
    status: "UP",
    timestamp: Date.now(),
  },

  // Market summary data
  marketSummary: {
    AAPL: {
      symbol: "AAPL",
      currentPrice: 150.25,
      change: 2.15,
      changePercent: 1.45,
      periodHigh: 155.0,
      periodLow: 145.0,
      dataPoints: 30,
      dataRange: "Last 30 days",
    },
    SPY: {
      symbol: "SPY",
      currentPrice: 425.8,
      change: -1.2,
      changePercent: -0.28,
      periodHigh: 430.0,
      periodLow: 415.5,
      dataPoints: 30,
      dataRange: "Last 30 days",
    },
    TSLA: {
      symbol: "TSLA",
      currentPrice: 180.95,
      change: 5.45,
      changePercent: 3.11,
      periodHigh: 185.0,
      periodLow: 165.0,
      dataPoints: 30,
      dataRange: "Last 30 days",
    },
  },

  // Available symbols
  availableSymbols: {
    symbols: ["AAPL", "SPY", "TSLA", "MSFT", "GOOGL", "AMZN", "META", "NVDA"],
    count: 8,
    message:
      "Basic symbol list. Upgrade to TRADER+ for extended symbol access.",
  },

  // Harmonic oscillator analysis
  harmonicOscillator: {
    symbol: "AAPL",
    amplitude: 5.23,
    frequency: 0.15,
    damping: 0.02,
    phase: 1.57,
    predictions: [
      { date: "2024-02-01", predictedPrice: 151.2 },
      { date: "2024-02-02", predictedPrice: 152.45 },
      { date: "2024-02-03", predictedPrice: 151.8 },
      { date: "2024-02-04", predictedPrice: 150.95 },
      { date: "2024-02-05", predictedPrice: 149.75 },
    ],
    supportLevels: [145.5, 148.25, 150.0],
    resistanceLevels: [152.75, 155.5, 158.0],
    tradingSignal: "BUY",
    confidence: 0.78,
    analysisTimestamp: "2024-01-31T10:30:00Z",
    dataStartDate: "2024-01-01",
    dataEndDate: "2024-01-31",
    dataPoints: 30,
  },

  // Wave physics analysis
  wavePhysics: {
    symbol: "AAPL",
    waveComponents: [
      {
        amplitude: 3.45,
        frequency: 0.12,
        phase: 0.78,
        period: "Daily cycle",
      },
      {
        amplitude: 2.15,
        frequency: 0.02,
        phase: 1.23,
        period: "Weekly cycle",
      },
      {
        amplitude: 1.8,
        frequency: 0.005,
        phase: 2.45,
        period: "Monthly cycle",
      },
    ],
    interferencePattern: "CONSTRUCTIVE",
    predictions: [
      { date: "2024-02-01", predictedPrice: 152.3 },
      { date: "2024-02-02", predictedPrice: 153.1 },
      { date: "2024-02-03", predictedPrice: 152.85 },
      { date: "2024-02-04", predictedPrice: 151.95 },
      { date: "2024-02-05", predictedPrice: 150.8 },
    ],
    tradingSignal: "HOLD",
    confidence: 0.82,
    analysisTimestamp: "2024-01-31T10:35:00Z",
    dataStartDate: "2024-01-10",
    dataEndDate: "2024-01-31",
    dataPoints: 21,
  },

  // Thermodynamics analysis
  thermodynamics: {
    symbol: "AAPL",
    temperature: 2.45,
    phaseState: "NORMAL",
    entropy: 1.23,
    heatCapacity: 0.89,
    tradingSignal: "SELL",
    confidence: 0.75,
    analysisTimestamp: "2024-01-31T10:40:00Z",
    dataStartDate: "2024-01-01",
    dataEndDate: "2024-01-31",
    dataPoints: 30,
  },

  // Error responses for testing error states
  errors: {
    invalidSymbol: {
      error: "VALIDATION_ERROR",
      message: "Symbol is required",
      timestamp: "2024-01-31T10:45:00Z",
    },
    symbolNotFound: {
      error: "NOT_FOUND",
      message: "No historical data found for symbol: XYZ",
      timestamp: "2024-01-31T10:45:00Z",
    },
    rateLimitExceeded: {
      error: "RATE_LIMIT_EXCEEDED",
      message: "Rate limit exceeded. Please try again later.",
      timestamp: "2024-01-31T10:45:00Z",
    },
  },
};

// Mock API client for Loveable development
export class MockAPIClient {
  constructor() {
    this.baseURL = "http://localhost:3001"; // Mock server URL for Loveable
  }

  // Simulate network delay
  async delay(ms = 500) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  // Health check
  async checkHealth() {
    await this.delay(200);
    return MOCK_API_RESPONSES.health;
  }

  // Market summary
  async getMarketSummary(symbol) {
    await this.delay(300);
    const data = MOCK_API_RESPONSES.marketSummary[symbol];
    if (!data) {
      throw new Error(`No data found for symbol: ${symbol}`);
    }
    return data;
  }

  // Available symbols
  async getAvailableSymbols() {
    await this.delay(250);
    return MOCK_API_RESPONSES.availableSymbols;
  }

  // Physics analysis methods
  async analyzeHarmonicOscillator(symbol, startDate = null, endDate = null) {
    await this.delay(800); // Longer delay to simulate calculation
    return {
      ...MOCK_API_RESPONSES.harmonicOscillator,
      symbol: symbol,
      analysisTimestamp: new Date().toISOString(),
    };
  }

  async analyzeWavePhysics(symbol) {
    await this.delay(1000);
    return {
      ...MOCK_API_RESPONSES.wavePhysics,
      symbol: symbol,
      analysisTimestamp: new Date().toISOString(),
    };
  }

  async analyzeThermodynamics(symbol, startDate, endDate) {
    await this.delay(700);
    return {
      ...MOCK_API_RESPONSES.thermodynamics,
      symbol: symbol,
      analysisTimestamp: new Date().toISOString(),
    };
  }
}

// Usage examples for Loveable components
export const USAGE_EXAMPLES = {
  // Example for market summary widget
  marketSummaryWidget: `
    import { MockAPIClient } from './mock-data/sample-responses.js';
    
    const mockAPI = new MockAPIClient();
    
    // In your component
    const [marketData, setMarketData] = useState(null);
    
    useEffect(() => {
      mockAPI.getMarketSummary('AAPL')
        .then(data => setMarketData(data))
        .catch(err => console.error(err));
    }, []);
  `,

  // Example for analysis component
  analysisComponent: `
    import { MockAPIClient } from './mock-data/sample-responses.js';
    
    const mockAPI = new MockAPIClient();
    
    const handleAnalyze = async (symbol, type) => {
      setLoading(true);
      try {
        let result;
        switch(type) {
          case 'harmonic':
            result = await mockAPI.analyzeHarmonicOscillator(symbol);
            break;
          case 'wave':
            result = await mockAPI.analyzeWavePhysics(symbol);
            break;
          case 'thermo':
            result = await mockAPI.analyzeThermodynamics(symbol, '2024-01-01', '2024-01-31');
            break;
        }
        setAnalysisResult(result);
      } catch (error) {
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };
  `,
};

// Chart data for visualization components
export const CHART_DATA = {
  priceHistory: [
    { date: "2024-01-01", price: 148.5 },
    { date: "2024-01-02", price: 149.25 },
    { date: "2024-01-03", price: 147.8 },
    { date: "2024-01-04", price: 150.15 },
    { date: "2024-01-05", price: 151.2 },
    { date: "2024-01-06", price: 149.95 },
    { date: "2024-01-07", price: 152.3 },
    { date: "2024-01-08", price: 150.75 },
    { date: "2024-01-09", price: 148.6 },
    { date: "2024-01-10", price: 150.25 },
  ],

  predictionChart: [
    { date: "2024-02-01", actual: null, predicted: 151.2, confidence: 0.85 },
    { date: "2024-02-02", actual: null, predicted: 152.45, confidence: 0.82 },
    { date: "2024-02-03", actual: null, predicted: 151.8, confidence: 0.79 },
    { date: "2024-02-04", actual: null, predicted: 150.95, confidence: 0.76 },
    { date: "2024-02-05", actual: null, predicted: 149.75, confidence: 0.73 },
  ],
};

export default MOCK_API_RESPONSES;
