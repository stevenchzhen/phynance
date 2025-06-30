export interface HarmonicOscillatorRequest {
  symbol: string;
  startDate: string; // ISO date string
  endDate: string; // ISO date string
  dampingFactor: number;
  frequency: number;
}
