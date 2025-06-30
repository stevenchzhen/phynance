import { useMutation, useQueryClient } from "@tanstack/react-query";
import { post } from "../api/apiClient";
import { PhysicsModelResult } from "../types/PhysicsModelResult";
import { HarmonicOscillatorRequest } from "../types/HarmonicOscillatorRequest";
import { ApiResponse } from "../types/ApiResponse";

export const usePhysicsModels = () => {
  const queryClient = useQueryClient();
  return useMutation<
    ApiResponse<PhysicsModelResult[]>,
    Error,
    HarmonicOscillatorRequest
  >(
    (body) =>
      post<ApiResponse<PhysicsModelResult[]>, HarmonicOscillatorRequest>(
        "/physics/harmonic-oscillator",
        body
      ),
    {
      onSuccess: (data, variables) => {
        // Optionally cache or invalidate related queries
        queryClient.invalidateQueries(["marketData", variables.symbol]);
      },
    }
  );
};
