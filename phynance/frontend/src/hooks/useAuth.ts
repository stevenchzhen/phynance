import { useMutation } from '@tanstack/react-query';

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export const useAuth = () => {
  const loginMutation = useMutation({
    mutationFn: async (body: LoginRequest): Promise<LoginResponse> => {
      // Mock authentication for testing - bypassing complex JWT for now
      await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate API call

      // Simple validation
      if (body.username === 'test@test.com' && body.password === 'password123') {
        return {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
        };
      } else {
        throw new Error('Invalid credentials');
      }
    },
    onSuccess: (data) => {
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
    },
    onError: () => {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    },
  });

  const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  };

  return {
    login: loginMutation.mutate,
    loginAsync: loginMutation.mutateAsync,
    isLoading: loginMutation.isPending,
    isSuccess: loginMutation.isSuccess,
    isError: loginMutation.isError,
    error: loginMutation.error,
    logout,
  };
};
