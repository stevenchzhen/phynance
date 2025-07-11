import { useQuery } from '@tanstack/react-query';
import { get } from '../api/apiClient';

export const useMarketData = (symbol: string, enabled = true) => {
  return useQuery<any, Error>(
    ['marketData', symbol],
    () => get<any>(`/viewer/market-summary/${encodeURIComponent(symbol)}`),
    {
      enabled: !!symbol && enabled,
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 30 * 60 * 1000, // 30 minutes
    },
  );
};
