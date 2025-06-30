import { useQuery } from "@tanstack/react-query";
import { get } from "../api/apiClient";
import { MarketDataDto } from "../types/MarketDataDto";
import { ApiResponse } from "../types/ApiResponse";

export const useMarketData = (symbol: string, enabled = true) => {
  return useQuery<ApiResponse<MarketDataDto[]>, Error>(
    ["marketData", symbol],
    () =>
      get<ApiResponse<MarketDataDto[]>>(
        `/market-data?symbol=${encodeURIComponent(symbol)}`
      ),
    {
      enabled: !!symbol && enabled,
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 30 * 60 * 1000, // 30 minutes
    }
  );
};
