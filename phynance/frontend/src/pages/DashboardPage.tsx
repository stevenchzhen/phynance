import React, { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  TextField,
  Button,
  Chip,
  Paper,
  Alert,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  Search as SearchIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Analytics as AnalyticsIcon,
  Waves as WavesIcon,
  Thermostat as ThermostatIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';
import { useMarketData } from '../hooks/useMarketData';
import api from '../api/apiClient';

interface PhysicsAnalysisResult {
  symbol: string;
  analysisType: string;
  dataRange: string;
  predictionDays: number;
  currentPrice: number;
  predictedPrices: number[];
  signals: string[];
  supportLevel: number;
  resistanceLevel: number;
  message: string;
}

interface WavePhysicsResult {
  symbol: string;
  supportLevels: number[];
  resistanceLevels: number[];
  nodeLevels: number[];
  amplitudes: number[];
  tradingSignals: string[];
  predictedLevels: number[];
  explanation: string;
}

interface ThermodynamicsResult {
  symbol: string;
  temperatureTrends: Array<{
    date: string;
    temperature: number;
  }>;
  phaseTransitions: Array<{
    date: string;
    type: string;
    description: string;
  }>;
  predictions: Array<{
    date: string;
    predictedTemperature: number;
    signal: string;
    comment: string;
  }>;
  metrics: {
    avgTemperature: number;
    entropy: number;
    heatCapacity: number;
  };
}

const DashboardPage: React.FC = () => {
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [advancedMode, setAdvancedMode] = useState<boolean>(false);

  // Physics analysis states
  const [physicsAnalysis, setPhysicsAnalysis] = useState<PhysicsAnalysisResult | null>(null);
  const [wavePhysicsAnalysis, setWavePhysicsAnalysis] = useState<WavePhysicsResult | null>(null);
  const [thermodynamicsAnalysis, setThermodynamicsAnalysis] = useState<ThermodynamicsResult | null>(
    null,
  );

  // Loading states
  const [physicsLoading, setPhysicsLoading] = useState<boolean>(false);
  const [wavePhysicsLoading, setWavePhysicsLoading] = useState<boolean>(false);
  const [thermodynamicsLoading, setThermodynamicsLoading] = useState<boolean>(false);

  // Error states
  const [physicsError, setPhysicsError] = useState<string | null>(null);
  const [wavePhysicsError, setWavePhysicsError] = useState<string | null>(null);
  const [thermodynamicsError, setThermodynamicsError] = useState<string | null>(null);

  const { data: marketData, isLoading: loading, error } = useMarketData(selectedSymbol);
  // marketData is now the direct response from /viewer/market-summary

  const popularSymbols = ['AAPL', 'SPY', 'TSLA', 'MSFT', 'GOOGL'];

  const handleSymbolSearch = () => {
    if (searchTerm.trim()) {
      setSelectedSymbol(searchTerm.trim().toUpperCase());
      setSearchTerm('');
    }
  };

  const handleSymbolSelect = (symbol: string) => {
    setSelectedSymbol(symbol);
  };

  // Analysis type change handler removed as it's not used

  const handlePhysicsAnalysis = async () => {
    if (!selectedSymbol) return;

    setPhysicsLoading(true);
    setPhysicsError(null);

    try {
      const response = await api.get(`/viewer/harmonic-oscillator/${selectedSymbol}`);
      setPhysicsAnalysis(response.data);
    } catch (err: any) {
      setPhysicsError(err.response?.data?.message || 'Failed to fetch physics analysis');
    } finally {
      setPhysicsLoading(false);
    }
  };

  const handleWavePhysicsAnalysis = async () => {
    if (!selectedSymbol) return;

    setWavePhysicsLoading(true);
    setWavePhysicsError(null);

    try {
      const requestData = {
        symbol: selectedSymbol,
        startDate: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 90 days ago
        endDate: new Date().toISOString().split('T')[0], // today
        predictionWeeks: 2,
      };

      const response = await api.post('/analysis/wave-physics', requestData);
      setWavePhysicsAnalysis(response.data);
    } catch (err: any) {
      setWavePhysicsError(err.response?.data?.message || 'Failed to fetch wave physics analysis');
    } finally {
      setWavePhysicsLoading(false);
    }
  };

  const handleThermodynamicsAnalysis = async () => {
    if (!selectedSymbol) return;

    setThermodynamicsLoading(true);
    setThermodynamicsError(null);

    try {
      const requestData = {
        symbol: selectedSymbol,
        startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 30 days ago
        endDate: new Date().toISOString().split('T')[0], // today
      };

      const response = await api.post('/analysis/thermodynamics', requestData);
      setThermodynamicsAnalysis(response.data);
    } catch (err: any) {
      setThermodynamicsError(
        err.response?.data?.message || 'Failed to fetch thermodynamics analysis',
      );
    } finally {
      setThermodynamicsLoading(false);
    }
  };

  const getSignalColor = (signal: string) => {
    switch (signal?.toUpperCase()) {
      case 'BUY':
        return 'success';
      case 'SELL':
        return 'error';
      case 'HOLD':
        return 'default';
      default:
        return 'default';
    }
  };

  const getSignalIcon = (signal: string) => {
    switch (signal?.toUpperCase()) {
      case 'BUY':
        return <TrendingUpIcon />;
      case 'SELL':
        return <TrendingDownIcon />;
      default:
        return <AnalyticsIcon />;
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        Physics-Based Financial Analytics
      </Typography>

      {/* Controls */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Grid container spacing={3} alignItems="center">
            <Grid item xs={12} md={4}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <TextField
                  label="Symbol"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSymbolSearch()}
                  size="small"
                  sx={{ flexGrow: 1 }}
                />
                <Button
                  variant="contained"
                  onClick={handleSymbolSearch}
                  startIcon={<SearchIcon />}
                  size="small"
                >
                  Search
                </Button>
              </Box>
            </Grid>

            <Grid item xs={12} md={4}>
              <Typography variant="body2" color="text.secondary">
                Popular Symbols:
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                {popularSymbols.map((symbol) => (
                  <Chip
                    key={symbol}
                    label={symbol}
                    variant={selectedSymbol === symbol ? 'filled' : 'outlined'}
                    onClick={() => handleSymbolSelect(symbol)}
                    size="small"
                  />
                ))}
              </Box>
            </Grid>

            <Grid item xs={12} md={4}>
              <FormControlLabel
                control={
                  <Switch
                    checked={advancedMode}
                    onChange={(e) => setAdvancedMode(e.target.checked)}
                  />
                }
                label="Advanced Physics Models"
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Market Data */}
      {loading ? (
        <Box display="flex" justifyContent="center" my={4}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error.message || 'An error occurred'}
        </Alert>
      ) : marketData ? (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h5" component="h2" gutterBottom>
              {selectedSymbol} Market Data
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6} md={3}>
                <Typography variant="body2" color="text.secondary">
                  Current Price
                </Typography>
                <Typography variant="h4" color="primary">
                  ${marketData.close?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Typography variant="body2" color="text.secondary">
                  High
                </Typography>
                <Typography variant="h5" color="success.main">
                  ${marketData.high?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Typography variant="body2" color="text.secondary">
                  Low
                </Typography>
                <Typography variant="h5" color="error.main">
                  ${marketData.low?.toFixed(2)}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Typography variant="body2" color="text.secondary">
                  Volume
                </Typography>
                <Typography variant="h5">{marketData.volume?.toLocaleString()}</Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      ) : null}

      {/* Physics Analysis */}
      <Grid container spacing={3}>
        {/* Harmonic Oscillator */}
        <Grid item xs={12} lg={4}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <TimelineIcon sx={{ mr: 1 }} />
                <Typography variant="h6">Harmonic Oscillator</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Damped harmonic oscillator model for price predictions
              </Typography>
              <Button
                variant="contained"
                onClick={handlePhysicsAnalysis}
                disabled={physicsLoading || !selectedSymbol}
                fullWidth
                sx={{ mb: 2 }}
              >
                {physicsLoading ? <CircularProgress size={20} /> : 'Analyze'}
              </Button>

              {physicsError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {physicsError}
                </Alert>
              )}

              {physicsAnalysis && (
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Analysis Results
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Support Level: ${physicsAnalysis.supportLevel?.toFixed(2)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Resistance Level: ${physicsAnalysis.resistanceLevel?.toFixed(2)}
                    </Typography>
                  </Box>

                  <Typography variant="subtitle2" gutterBottom>
                    5-Day Predictions
                  </Typography>
                  <List dense>
                    {physicsAnalysis.predictedPrices?.map((price, index) => (
                      <ListItem key={index} sx={{ px: 0 }}>
                        <ListItemIcon>{getSignalIcon(physicsAnalysis.signals[index])}</ListItemIcon>
                        <ListItemText
                          primary={`Day ${index + 1}: $${price.toFixed(2)}`}
                          secondary={
                            <Chip
                              label={physicsAnalysis.signals[index]}
                              color={getSignalColor(physicsAnalysis.signals[index])}
                              size="small"
                            />
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </Paper>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Wave Physics */}
        {advancedMode && (
          <Grid item xs={12} lg={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <WavesIcon sx={{ mr: 1 }} />
                  <Typography variant="h6">Wave Physics</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Wave interference patterns and cycle analysis
                </Typography>
                <Button
                  variant="contained"
                  onClick={handleWavePhysicsAnalysis}
                  disabled={wavePhysicsLoading || !selectedSymbol}
                  fullWidth
                  sx={{ mb: 2 }}
                >
                  {wavePhysicsLoading ? <CircularProgress size={20} /> : 'Analyze Waves'}
                </Button>

                {wavePhysicsError && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {wavePhysicsError}
                  </Alert>
                )}

                {wavePhysicsAnalysis && (
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Wave Analysis Results
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        Support Levels: {wavePhysicsAnalysis.supportLevels?.length || 0}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Resistance Levels: {wavePhysicsAnalysis.resistanceLevels?.length || 0}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Node Levels: {wavePhysicsAnalysis.nodeLevels?.length || 0}
                      </Typography>
                    </Box>

                    <Typography variant="subtitle2" gutterBottom>
                      Wave Amplitudes
                    </Typography>
                    <List dense>
                      {wavePhysicsAnalysis.amplitudes?.map((amplitude, index) => (
                        <ListItem key={index} sx={{ px: 0 }}>
                          <ListItemText
                            primary={`Wave ${index + 1}: ${amplitude.toFixed(4)}`}
                            secondary={
                              index === 0 ? 'Short-term' : index === 1 ? 'Medium-term' : 'Long-term'
                            }
                          />
                        </ListItem>
                      ))}
                    </List>

                    <Typography variant="subtitle2" gutterBottom>
                      Recent Signals
                    </Typography>
                    <List dense>
                      {wavePhysicsAnalysis.tradingSignals?.slice(-3).map((signal, index) => (
                        <ListItem key={index} sx={{ px: 0 }}>
                          <ListItemText primary={signal} secondary={`Signal ${index + 1}`} />
                        </ListItem>
                      ))}
                    </List>
                  </Paper>
                )}
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Thermodynamics */}
        {advancedMode && (
          <Grid item xs={12} lg={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <ThermostatIcon sx={{ mr: 1 }} />
                  <Typography variant="h6">Thermodynamics</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Market temperature and phase transition analysis
                </Typography>
                <Button
                  variant="contained"
                  onClick={handleThermodynamicsAnalysis}
                  disabled={thermodynamicsLoading || !selectedSymbol}
                  fullWidth
                  sx={{ mb: 2 }}
                >
                  {thermodynamicsLoading ? <CircularProgress size={20} /> : 'Analyze Temperature'}
                </Button>

                {thermodynamicsError && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {thermodynamicsError}
                  </Alert>
                )}

                {thermodynamicsAnalysis && (
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Thermodynamic Analysis
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        Avg Temperature:{' '}
                        {thermodynamicsAnalysis.metrics?.avgTemperature?.toFixed(2)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Entropy: {thermodynamicsAnalysis.metrics?.entropy?.toFixed(4)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Heat Capacity: {thermodynamicsAnalysis.metrics?.heatCapacity?.toFixed(2)}
                      </Typography>
                    </Box>

                    <Typography variant="subtitle2" gutterBottom>
                      Phase Transitions
                    </Typography>
                    <List dense>
                      {thermodynamicsAnalysis.phaseTransitions?.map((transition, index) => (
                        <ListItem key={index} sx={{ px: 0 }}>
                          <ListItemText
                            primary={transition.type}
                            secondary={transition.description}
                          />
                        </ListItem>
                      ))}
                    </List>

                    <Typography variant="subtitle2" gutterBottom>
                      Recent Predictions
                    </Typography>
                    <List dense>
                      {thermodynamicsAnalysis.predictions?.slice(-3).map((prediction, index) => (
                        <ListItem key={index} sx={{ px: 0 }}>
                          <ListItemText
                            primary={
                              <Chip
                                label={prediction.signal}
                                color={getSignalColor(prediction.signal)}
                                size="small"
                              />
                            }
                            secondary={prediction.comment}
                          />
                        </ListItem>
                      ))}
                    </List>
                  </Paper>
                )}
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Container>
  );
};

export default DashboardPage;
