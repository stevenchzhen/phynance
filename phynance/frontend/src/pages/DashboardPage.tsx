import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Button,
  Alert,
  TextField,
  InputAdornment,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  CircularProgress,
} from '@mui/material';
import { Search as SearchIcon, TrendingUp, Analytics, Science } from '@mui/icons-material';
import { useAuthContext } from '../context/AuthContext';
import { get } from '../api/apiClient';

// Types
interface MarketData {
  symbol: string;
  currentPrice: number;
  change: number;
  changePercent: number;
  periodHigh: number;
  periodLow: number;
  volume?: number;
  timestamp?: string;
}

interface PhysicsModel {
  name: string;
  description: string;
  icon: React.ReactNode;
  endpoint: string;
  requiresAuth: boolean;
}

const DashboardPage: React.FC = () => {
  const { user, logout } = useAuthContext();
  const [symbol, setSymbol] = useState('AAPL');
  const [marketData, setMarketData] = useState<MarketData | null>(null);
  const [availableSymbols, setAvailableSymbols] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [physicsAnalysis, setPhysicsAnalysis] = useState<any | null>(null);
  const [physicsLoading, setPhysicsLoading] = useState(false);
  const [physicsError, setPhysicsError] = useState<string | null>(null);

  // Physics models available
  const physicsModels: PhysicsModel[] = [
    {
      name: 'Harmonic Oscillator',
      description: 'Predicts price reversals using damped oscillation patterns',
      icon: <Science color="primary" />,
      endpoint: '/gateway/analysis/harmonic-oscillator',
      requiresAuth: true,
    },
    {
      name: 'Market Temperature',
      description: 'Thermodynamic analysis of market volatility and regime changes',
      icon: <Analytics color="secondary" />,
      endpoint: '/gateway/analysis/thermodynamics',
      requiresAuth: true,
    },
    {
      name: 'Wave Interference',
      description: 'Support/resistance levels through wave physics principles',
      icon: <TrendingUp color="success" />,
      endpoint: '/gateway/analysis/wave-physics',
      requiresAuth: true,
    },
  ];

  // Load available symbols on component mount
  useEffect(() => {
    const loadAvailableSymbols = async () => {
      try {
        const response = await get<{ symbols: string[] }>('/viewer/available-symbols');
        setAvailableSymbols(response.symbols);
      } catch (error) {
        console.error('Failed to load available symbols:', error);
        // Fallback to default symbols
        setAvailableSymbols(['AAPL', 'GOOGL', 'MSFT', 'TSLA', 'AMZN']);
      }
    };

    loadAvailableSymbols();
  }, []);

  // Load market data for selected symbol
  const loadMarketData = async (selectedSymbol: string) => {
    if (!selectedSymbol) return;

    setLoading(true);
    setError(null);

    try {
      const response = await get<MarketData>(`/viewer/market-summary/${selectedSymbol}`);
      setMarketData(response);
    } catch (error) {
      console.error('Failed to load market data:', error);
      setError('Failed to load market data for ' + selectedSymbol);
    } finally {
      setLoading(false);
    }
  };

  // Load physics analysis for selected symbol
  const handlePhysicsAnalysis = async (selectedSymbol: string) => {
    if (!selectedSymbol) return;

    setPhysicsLoading(true);
    setPhysicsError(null);

    try {
      const response = await get<any>(`/viewer/harmonic-oscillator/${selectedSymbol}`);
      setPhysicsAnalysis(response);
    } catch (error) {
      console.error('Failed to load physics analysis:', error);
      setPhysicsError('Failed to load physics analysis for ' + selectedSymbol);
    } finally {
      setPhysicsLoading(false);
    }
  };

  // Load initial data
  useEffect(() => {
    loadMarketData(symbol);
  }, [symbol]);

  // Handle search
  const handleSearch = () => {
    if (searchTerm.trim()) {
      setSymbol(searchTerm.toUpperCase());
      setSearchTerm('');
    }
  };

  // Handle symbol selection from popular symbols
  const handleSymbolSelect = (selectedSymbol: string) => {
    setSymbol(selectedSymbol);
  };

  // Handle logout
  const handleLogout = async () => {
    await logout();
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Physics Finance Dashboard
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Welcome back, {user?.username}!
          </Typography>
        </Box>
        <Button variant="outlined" color="secondary" onClick={handleLogout}>
          Logout
        </Button>
      </Box>

      {/* Search Bar */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Search for a stock symbol (e.g., AAPL, GOOGL)"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
            <Button variant="contained" onClick={handleSearch} disabled={!searchTerm.trim()}>
              Search
            </Button>
          </Box>

          {/* Popular Symbols */}
          <Box mt={2}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Popular Symbols:
            </Typography>
            <Box display="flex" flexWrap="wrap" gap={1}>
              {availableSymbols.map((sym) => (
                <Chip
                  key={sym}
                  label={sym}
                  variant={symbol === sym ? 'filled' : 'outlined'}
                  color={symbol === sym ? 'primary' : 'default'}
                  onClick={() => handleSymbolSelect(sym)}
                  size="small"
                />
              ))}
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Error Display */}
      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={4}>
        {/* Market Data Panel */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Market Data - {symbol}
              </Typography>

              {loading ? (
                <Box display="flex" justifyContent="center" py={4}>
                  <CircularProgress />
                </Box>
              ) : marketData ? (
                <TableContainer>
                  <Table size="small">
                    <TableBody>
                      <TableRow>
                        <TableCell>
                          <strong>Symbol</strong>
                        </TableCell>
                        <TableCell>{marketData.symbol}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <strong>Current Price</strong>
                        </TableCell>
                        <TableCell>${marketData.currentPrice.toFixed(2)}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <strong>Change</strong>
                        </TableCell>
                        <TableCell style={{ color: marketData.change >= 0 ? 'green' : 'red' }}>
                          {marketData.change >= 0 ? '+' : ''}${marketData.change.toFixed(2)} (
                          {marketData.changePercent.toFixed(2)}%)
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <strong>Period High</strong>
                        </TableCell>
                        <TableCell>${marketData.periodHigh.toFixed(2)}</TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell>
                          <strong>Period Low</strong>
                        </TableCell>
                        <TableCell>${marketData.periodLow.toFixed(2)}</TableCell>
                      </TableRow>
                      {marketData.volume && (
                        <TableRow>
                          <TableCell>
                            <strong>Volume</strong>
                          </TableCell>
                          <TableCell>{marketData.volume.toLocaleString()}</TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  No market data available
                </Typography>
              )}

              <Box mt={2}>
                <Button
                  variant="outlined"
                  onClick={() => loadMarketData(symbol)}
                  disabled={loading}
                  fullWidth
                >
                  Refresh Data
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Physics Models Panel */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Physics-Based Analysis Models
              </Typography>

              <Box display="flex" flexDirection="column" gap={2}>
                {physicsModels.map((model) => (
                  <Paper key={model.name} elevation={1} sx={{ p: 2 }}>
                    <Box display="flex" alignItems="center" gap={2}>
                      {model.icon}
                      <Box flex={1}>
                        <Typography variant="subtitle1" fontWeight="bold">
                          {model.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {model.description}
                        </Typography>
                      </Box>
                      <Button
                        variant="contained"
                        size="small"
                        disabled={
                          !marketData || (model.name === 'Harmonic Oscillator' && physicsLoading)
                        }
                        onClick={() => {
                          if (model.name === 'Harmonic Oscillator') {
                            handlePhysicsAnalysis(symbol);
                          } else {
                            alert(
                              `${
                                model.name
                              } analysis coming soon! Will analyze ${symbol} using ${model.name.toLowerCase()} physics model.`,
                            );
                          }
                        }}
                      >
                        {model.name === 'Harmonic Oscillator' && physicsLoading
                          ? 'Analyzing...'
                          : 'Analyze'}
                      </Button>
                    </Box>
                  </Paper>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Physics Analysis Results Panel */}
        {physicsAnalysis && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Physics Analysis Results - {physicsAnalysis.symbol}
                </Typography>

                {physicsError && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {physicsError}
                  </Alert>
                )}

                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Paper elevation={1} sx={{ p: 2 }}>
                      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                        Support & Resistance Levels
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Support: ${physicsAnalysis.supportLevel?.toFixed(2) || 'N/A'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Resistance: ${physicsAnalysis.resistanceLevel?.toFixed(2) || 'N/A'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Current: ${physicsAnalysis.currentPrice?.toFixed(2) || 'N/A'}
                      </Typography>
                    </Paper>
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <Paper elevation={1} sx={{ p: 2 }}>
                      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                        Predicted Prices (Next 5 Days)
                      </Typography>
                      {physicsAnalysis.predictedPrices?.map((price: number, index: number) => (
                        <Typography key={index} variant="body2" color="text.secondary">
                          Day {index + 1}: ${price.toFixed(2)}
                        </Typography>
                      ))}
                    </Paper>
                  </Grid>

                  <Grid item xs={12}>
                    <Paper elevation={1} sx={{ p: 2 }}>
                      <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                        Trading Signals
                      </Typography>
                      <Box display="flex" flexWrap="wrap" gap={1}>
                        {physicsAnalysis.signals?.map((signal: string, index: number) => (
                          <Chip
                            key={index}
                            label={`Day ${index + 1}: ${signal}`}
                            color={
                              signal === 'BUY' ? 'success' : signal === 'SELL' ? 'error' : 'default'
                            }
                            size="small"
                          />
                        ))}
                      </Box>
                    </Paper>
                  </Grid>
                </Grid>

                <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                  {physicsAnalysis.message}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* System Status Panel */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                System Status
              </Typography>

              <Grid container spacing={3}>
                <Grid item xs={12} sm={4}>
                  <Box textAlign="center">
                    <Typography variant="h4" color="primary">
                      ✓
                    </Typography>
                    <Typography variant="body2">API Gateway Connected</Typography>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Box textAlign="center">
                    <Typography variant="h4" color="success.main">
                      ✓
                    </Typography>
                    <Typography variant="body2">Market Data Service</Typography>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Box textAlign="center">
                    <Typography variant="h4" color="warning.main">
                      ⚡
                    </Typography>
                    <Typography variant="body2">Physics Models Ready</Typography>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default DashboardPage;
