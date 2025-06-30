import React from "react";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface ChartPanelProps {
  data: number[];
  labels: string[];
  title?: string;
}

const ChartPanel: React.FC<ChartPanelProps> = ({ data, labels, title }) => {
  const chartData = {
    labels,
    datasets: [
      {
        label: title || "Financial Data",
        data,
        borderColor: "rgba(75,192,192,1)",
        backgroundColor: "rgba(75,192,192,0.2)",
      },
    ],
  };

  return <Line data={chartData} />;
};

export default ChartPanel;
