import { useQuery } from "@tanstack/react-query";
import { get } from "../api/apiClient";
import { useState, useEffect } from "react";

export interface StockSearchResult {
  symbol: string;
  name: string;
}

export const useStockSearch = (query: string, debounceMs = 400) => {
  const [debounced, setDebounced] = useState(query);
  useEffect(() => {
    const handler = setTimeout(() => setDebounced(query), debounceMs);
    return () => clearTimeout(handler);
  }, [query, debounceMs]);

  return useQuery<StockSearchResult[], Error>(
    ["stockSearch", debounced],
    () =>
      get<StockSearchResult[]>(
        `/stocks/search?q=${encodeURIComponent(debounced)}`
      ),
    {
      enabled: !!debounced,
      staleTime: 10 * 60 * 1000,
    }
  );
};
