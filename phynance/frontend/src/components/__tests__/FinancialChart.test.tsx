import React from 'react';
import { render, screen } from '../../test-utils';
import FinancialChart from '../FinancialChart';
import { MarketDataDto } from '../../types/MarketDataDto';
import { PhysicsModelResult } from '../../types/PhysicsModelResult';

describe('FinancialChart', () => {
  it('renders loading skeleton', () => {
    render(<FinancialChart data={[]} loading />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders empty state', () => {
    render(<FinancialChart data={[]} loading={false} />);
    expect(screen.getByText(/no data available/i)).toBeInTheDocument();
  });

  it('renders chart with data', () => {
    const data: MarketDataDto[] = [
      {
        id: 1,
        symbol: 'AAPL',
        timestamp: '2024-01-01T00:00:00Z',
        open: 100,
        high: 110,
        low: 95,
        close: 105,
        volume: 10000,
      },
    ];
    const modelResults: PhysicsModelResult[] = [
      { timestamp: '2024-01-01T00:00:00Z', value: 104, modelType: 'harmonic' },
    ];
    render(<FinancialChart data={data} modelResults={modelResults} />);
    expect(screen.getByText(/price/i)).toBeInTheDocument();
    expect(screen.getByText(/model/i)).toBeInTheDocument();
  });
});
