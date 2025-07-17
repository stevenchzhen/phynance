# Phynance Loveable Frontend

This directory contains UI components and pages designed using **Loveable** for the Phynance physics-based financial analysis platform.

## 🎯 Purpose

This frontend is specifically for **UI/UX development** using Loveable's visual design tools. The components created here will be integrated with the production frontend that connects to the Spring Boot backend.

## 📁 Directory Structure

```
frontend-loveable/
├── components/           # React components from Loveable
│   ├── Dashboard/       # Main dashboard components
│   ├── Analysis/        # Physics analysis forms and results
│   ├── Charts/          # Visualization components
│   └── Common/          # Shared UI components
├── pages/               # Page layouts
│   ├── HomePage.jsx
│   ├── AnalysisPage.jsx
│   └── DashboardPage.jsx
├── assets/              # Images, icons, styles
├── mock-data/           # Sample data for development
│   └── sample-responses.js
└── README.md           # This file
```

## 🚀 Getting Started

### 1. Set Up Loveable Project

1. **Create New Project** in Loveable
2. **Import Components**: Use the designs from this directory as reference
3. **Use Mock Data**: Import `mock-data/sample-responses.js` for realistic data

### 2. Development Workflow

#### **Phase 1: Design in Loveable**

```bash
# Use Loveable's visual interface to create:
# 1. Dashboard layout with stock summaries
# 2. Physics analysis forms (Harmonic, Wave, Thermodynamics)
# 3. Chart visualization components
# 4. Navigation and user interface elements
```

#### **Phase 2: Export Components**

```bash
# Export React components from Loveable
# Save them in this directory structure:
frontend-loveable/
├── components/
│   ├── StockDashboard.jsx
│   ├── PhysicsAnalysisForm.jsx
│   ├── ResultsChart.jsx
│   └── NavigationMenu.jsx
```

#### **Phase 3: Integration Testing**

```bash
# Test components with mock data
npm install
npm start

# Components should work with mock APIs
# Before integrating with real backend
```

## 🔌 API Integration

### Mock API Usage (For Loveable Development)

```javascript
import {
  MockAPIClient,
  MOCK_API_RESPONSES,
} from "./mock-data/sample-responses.js";

const mockAPI = new MockAPIClient();

// Example usage in components
const [marketData, setMarketData] = useState(null);
const [loading, setLoading] = useState(false);

const fetchMarketData = async (symbol) => {
  setLoading(true);
  try {
    const data = await mockAPI.getMarketSummary(symbol);
    setMarketData(data);
  } catch (error) {
    console.error("Error:", error);
  } finally {
    setLoading(false);
  }
};
```

### Real API Integration (For Production)

When moving to production, replace mock calls with real API calls:

```javascript
// Replace MockAPIClient with real API client
import { PhynanceAPIClient } from "../integration/api-client.js";

const realAPI = new PhynanceAPIClient("http://localhost:8081");
```

## 🎨 Design Guidelines

### Component Requirements

#### **1. Stock Dashboard**

- **Display**: Current price, change, percentage change
- **Features**: Multiple stock symbols, refresh functionality
- **Mock Data**: Use `MOCK_API_RESPONSES.marketSummary`

#### **2. Physics Analysis Forms**

- **Harmonic Oscillator**: Symbol input, date range (optional)
- **Wave Physics**: Symbol input only
- **Thermodynamics**: Symbol input, date range (required)
- **Mock Data**: Use respective analysis response objects

#### **3. Results Visualization**

- **Charts**: Price predictions, confidence levels
- **Tables**: Support/resistance levels, trading signals
- **Indicators**: Signal strength, analysis confidence

#### **4. Navigation & Layout**

- **Responsive**: Works on desktop and mobile
- **Clean UI**: Material Design or similar modern framework
- **Loading States**: Spinners for API calls

### Styling Recommendations

```css
/* Use these color schemes for consistency */
:root {
  --primary-color: #1976d2; /* Blue for buy signals */
  --secondary-color: #388e3c; /* Green for positive changes */
  --danger-color: #d32f2f; /* Red for sell signals */
  --warning-color: #f57c00; /* Orange for hold signals */
  --background: #f5f5f5; /* Light gray background */
  --text-primary: #212121; /* Dark text */
  --text-secondary: #757575; /* Gray text */
}
```

## 📊 Available Mock Data

### Market Summary

```javascript
// Available symbols with mock data
AAPL, SPY, TSLA, MSFT, GOOGL, AMZN, META, NVDA

// Each includes:
{
  symbol: "AAPL",
  currentPrice: 150.25,
  change: 2.15,
  changePercent: 1.45,
  periodHigh: 155.00,
  periodLow: 145.00
}
```

### Physics Analysis Results

```javascript
// Harmonic Oscillator
{
  amplitude: 5.23,
  frequency: 0.15,
  predictions: [...],
  tradingSignal: "BUY",
  confidence: 0.78
}

// Wave Physics
{
  waveComponents: [...],
  interferencePattern: "CONSTRUCTIVE",
  tradingSignal: "HOLD"
}

// Thermodynamics
{
  temperature: 2.45,
  phaseState: "NORMAL",
  tradingSignal: "SELL"
}
```

## 🔄 Integration with Production

### Step 1: Export from Loveable

```bash
# Download/export React components from Loveable
# Place them in this directory structure
```

### Step 2: Test with Mock Data

```bash
cd frontend-loveable
npm install
npm start

# Verify all components work with mock data
# Test different symbols and analysis types
```

### Step 3: Move to Production

```bash
# Copy tested components to main frontend
cp -r frontend-loveable/components/* phynance/frontend/src/components/

# Update API calls from mock to real backend
# Test integration with Spring Boot backend
```

## 🛠️ Development Commands

```bash
# Install dependencies
npm install

# Start development server with mock data
npm start

# Run in Loveable preview mode
npm run loveable:preview

# Export components for production
npm run export:components

# Test component integration
npm run test:integration
```

## 📋 Component Checklist

### Required Components for V1

- [ ] **StockSelector**: Dropdown/search for stock symbols
- [ ] **MarketSummaryCard**: Display current price and changes
- [ ] **PhysicsModelSelector**: Toggle between analysis types
- [ ] **AnalysisForm**: Input form for analysis parameters
- [ ] **LoadingSpinner**: Show during API calls
- [ ] **ErrorMessage**: Display API errors gracefully
- [ ] **ResultsChart**: Visualize predictions and signals
- [ ] **TradingSignalBadge**: Display BUY/SELL/HOLD signals
- [ ] **ConfidenceIndicator**: Show analysis confidence levels
- [ ] **PredictionTable**: List future price predictions

### Advanced Components for V2

- [ ] **AdvancedChart**: Interactive charts with zoom/pan
- [ ] **ComparisonView**: Side-by-side analysis results
- [ ] **HistoricalBacktest**: Test predictions against actual data
- [ ] **AlertSystem**: Set up price/signal alerts
- [ ] **PortfolioView**: Multiple stock analysis dashboard

## 🚦 Ready for Integration

Your components are ready for production integration when:

1. ✅ All components work with mock data
2. ✅ Loading and error states are handled
3. ✅ Responsive design works on all screen sizes
4. ✅ No console errors during development
5. ✅ Components follow consistent design patterns

## 🤝 Working with Cursor

Once UI design is complete in Loveable:

1. **Export Components** to this directory
2. **Document Requirements** for any new backend APIs needed
3. **Test Integration** using existing Spring Boot backend
4. **Optimize Performance** in Cursor for production deployment

For backend development and API implementation, switch to **Cursor** and work in the `phynance/backend-spring/` directory.

---

**Happy Building! 🎨💻**
