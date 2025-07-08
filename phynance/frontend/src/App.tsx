import React, { useMemo, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  ThemeProvider,
  CssBaseline,
  createTheme,
  Snackbar,
  Alert,
  CircularProgress,
  Box,
} from '@mui/material';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoginForm from './components/auth/LoginForm';
import DashboardPage from './pages/DashboardPage';
// import AnalysisPage from './pages/AnalysisPage';
// import PortfolioPage from './pages/PortfolioPage';
// import SettingsPage from './pages/SettingsPage';

// --- Error Boundary ---
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: any }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error: any) {
    return { hasError: true, error };
  }
  componentDidCatch(error: any, errorInfo: any) {
    // Log error
    // eslint-disable-next-line no-console
    console.error(error, errorInfo);
  }
  render() {
    if (this.state.hasError) {
      return (
        <Box p={4}>
          <Alert severity="error">{this.state.error?.message || 'Something went wrong.'}</Alert>
        </Box>
      );
    }
    return this.props.children;
  }
}

// --- React Query Client ---
const queryClient = new QueryClient();

// --- Main App ---
const App: React.FC = () => {
  const [mode] = useState<'light' | 'dark'>('light');
  const [notification, setNotification] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info' | 'warning';
  }>({ open: false, message: '', severity: 'info' });
  const [globalLoading] = useState(false);

  const theme = useMemo(() => createTheme({ palette: { mode } }), [mode]);
  const handleCloseNotification = () => setNotification((n) => ({ ...n, open: false }));

  // Example: setGlobalLoading(true/false) and setNotification({open:true, message:'...', severity:'success'}) from anywhere via context or props

  return (
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <ErrorBoundary>
            <AuthProvider>
              <Router>
                {globalLoading && (
                  <Box
                    position="fixed"
                    top={0}
                    left={0}
                    width="100vw"
                    height="100vh"
                    zIndex={2000}
                    display="flex"
                    alignItems="center"
                    justifyContent="center"
                    bgcolor="rgba(255,255,255,0.5)"
                  >
                    <CircularProgress />
                  </Box>
                )}
                <Snackbar
                  open={notification.open}
                  autoHideDuration={4000}
                  onClose={handleCloseNotification}
                  anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
                >
                  <Alert
                    onClose={handleCloseNotification}
                    severity={notification.severity}
                    sx={{ width: '100%' }}
                  >
                    {notification.message}
                  </Alert>
                </Snackbar>
                <Routes>
                  <Route path="/login" element={<LoginForm />} />
                  <Route path="/" element={<Navigate to="/dashboard" replace />} />
                  <Route element={<ProtectedRoute />}>
                    <Route path="/dashboard" element={<DashboardPage />} />
                    {/* Example protected routes: */}
                    {/* <Route path="/analysis" element={<DashboardLayout><AnalysisPage /></DashboardLayout>} /> */}
                    {/* <Route path="/portfolio" element={<DashboardLayout><PortfolioPage /></DashboardLayout>} /> */}
                    {/* <Route path="/settings" element={<DashboardLayout><SettingsPage /></DashboardLayout>} /> */}
                  </Route>
                </Routes>
              </Router>
            </AuthProvider>
          </ErrorBoundary>
        </ThemeProvider>
      </QueryClientProvider>
    </React.StrictMode>
  );
};

export default App;
