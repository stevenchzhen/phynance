import { useMutation } from "@tanstack/react-query";
import { post } from "../api/apiClient";

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export const useAuth = () => {
  const loginMutation = useMutation<LoginResponse, Error, LoginRequest>(
    (body) => post<LoginResponse, LoginRequest>("/auth/login", body),
    {
      onSuccess: (data) => {
        localStorage.setItem("access_token", data.accessToken);
        localStorage.setItem("refresh_token", data.refreshToken);
      },
      onError: () => {
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");
      },
    }
  );

  const logout = () => {
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
  };

  return {
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    isLoading: loginMutation.isLoading,
    isSuccess: loginMutation.isSuccess,
    isError: loginMutation.isError,
    error: loginMutation.error,
    logout,
  };
};
