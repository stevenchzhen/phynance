import React, { useMemo, useRef, useState } from "react";
import { Chart, ChartData, ChartOptions, registerables } from "chart.js";
import { Chart as ReactChart } from "react-chartjs-2";
import "chartjs-adapter-date-fns";
import { OhlcController, CandlestickController } from "chartjs-chart-financial";
import zoomPlugin from "chartjs-plugin-zoom";
import {
  Box,
  Skeleton,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
  useTheme,
} from "@mui/material";
import type { MarketDataDto } from "../types/MarketDataDto";
import type { PhysicsModelResult } from "../types/PhysicsModelResult";

Chart.register(
  ...registerables,
  OhlcController,
  CandlestickController,
  zoomPlugin
);

export interface FinancialChartProps {
  data: MarketDataDto[];
  modelResults?: PhysicsModelResult[];
  loading?: boolean;
  error?: string | null;
}

const TIMEFRAMES = [
  { label: "1D", value: "1D" },
  { label: "1W", value: "1W" },
  { label: "1M", value: "1M" },
  { label: "1Y", value: "1Y" },
];

function filterDataByTimeframe(
  data: MarketDataDto[],
  timeframe: string
): MarketDataDto[] {
  if (!data.length) return [];
  const end = new Date(data[data.length - 1].timestamp).getTime();
  let start: number;
  switch (timeframe) {
    case "1D":
      start = end - 24 * 60 * 60 * 1000;
      break;
    case "1W":
      start = end - 7 * 24 * 60 * 60 * 1000;
      break;
    case "1M":
      start = end - 30 * 24 * 60 * 60 * 1000;
      break;
    case "1Y":
      start = end - 365 * 24 * 60 * 60 * 1000;
      break;
    default:
      return data;
  }
  return data.filter((d) => new Date(d.timestamp).getTime() >= start);
}

const FinancialChart: React.FC<FinancialChartProps> = ({
  data,
  modelResults,
  loading,
  error,
}) => {
  const [timeframe, setTimeframe] = useState("1M");
  const theme = useTheme();
  const chartRef = useRef<Chart<"candlestick">>(null);

  const filteredData = useMemo(
    () => filterDataByTimeframe(data, timeframe),
    [data, timeframe]
  );
  const filteredModel = useMemo(
    () =>
      modelResults?.filter((m) =>
        filteredData.some((d) => d.timestamp === m.timestamp)
      ),
    [modelResults, filteredData]
  );

  const chartData: ChartData<"candlestick"> = useMemo(
    () => ({
      datasets: [
        {
          label: "Price",
          data: filteredData.map((d) => ({
            x: d.timestamp,
            o: d.open,
            h: d.high,
            l: d.low,
            c: d.close,
          })),
          type: "candlestick",
          borderColor: theme.palette.primary.main,
          backgroundColor: theme.palette.primary.light,
        },
        filteredModel && filteredModel.length > 0
          ? {
              label: "Model",
              data: filteredModel.map((m) => ({ x: m.timestamp, y: m.value })),
              type: "line",
              borderColor: theme.palette.secondary.main,
              backgroundColor: theme.palette.secondary.light,
              pointRadius: 0,
              borderWidth: 2,
              fill: false,
              yAxisID: "y",
            }
          : undefined,
      ].filter(Boolean) as any,
    }),
    [filteredData, filteredModel, theme]
  );

  const options: ChartOptions<"candlestick"> = useMemo(
    () => ({
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true },
        tooltip: { mode: "index", intersect: false },
        zoom: {
          pan: { enabled: true, mode: "x" },
          zoom: {
            wheel: { enabled: true },
            pinch: { enabled: true },
            mode: "x",
          },
          limits: { x: { minRange: 1 } },
        },
        crosshair: {
          enabled: true,
          line: { color: theme.palette.divider, width: 1 },
        },
      },
      scales: {
        x: {
          type: "time",
          time: { unit: "day" },
          title: { display: true, text: "Time" },
          grid: { color: theme.palette.divider },
        },
        y: {
          position: "left",
          title: { display: true, text: "Price" },
          grid: { color: theme.palette.divider },
        },
      },
    }),
    [theme]
  );

  if (loading) {
    return <Skeleton variant="rectangular" width="100%" height={400} />;
  }
  if (error) {
    return (
      <Box p={2}>
        <Typography color="error">{error}</Typography>
      </Box>
    );
  }
  if (!filteredData.length) {
    return (
      <Box p={2}>
        <Typography>No data available.</Typography>
      </Box>
    );
  }

  return (
    <Box width="100%" maxWidth="100%" height={{ xs: 300, sm: 400, md: 500 }}>
      <Box display="flex" justifyContent="flex-end" mb={1}>
        <ToggleButtonGroup
          value={timeframe}
          exclusive
          onChange={(_, v) => v && setTimeframe(v)}
          size="small"
        >
          {TIMEFRAMES.map((tf) => (
            <ToggleButton key={tf.value} value={tf.value}>
              {tf.label}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>
      </Box>
      <ReactChart
        ref={chartRef}
        type="candlestick"
        data={chartData}
        options={options}
        style={{ width: "100%", height: "100%" }}
      />
    </Box>
  );
};

export default FinancialChart;
