import { useQuery } from "@tanstack/react-query";
import axios from "axios";

export const useFinancialData = () => {
  return useQuery(["financialData"], async () => {
    const { data } = await axios.get("/api/financial-data");
    return data;
  });
};
