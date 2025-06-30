export interface MarketDataDto {
  id: number;
  symbol: string;
  timestamp: string; // ISO date string
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}
