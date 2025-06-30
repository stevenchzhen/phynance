import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
} from "axios";

// --- Token Storage Helpers (customize as needed) ---
const getAccessToken = () => localStorage.getItem("access_token");
const getRefreshToken = () => localStorage.getItem("refresh_token");
const setAccessToken = (token: string) =>
  localStorage.setItem("access_token", token);
const setRefreshToken = (token: string) =>
  localStorage.setItem("refresh_token", token);
const clearTokens = () => {
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
};

// --- Axios Instance ---
const api: AxiosInstance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  timeout: 10000, // 10 seconds
  withCredentials: true, // for CORS cookies if needed
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// --- Request Interceptor: Attach JWT ---
api.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token && config.headers) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    // Logging
    if (process.env.NODE_ENV === "development") {
      console.log(
        "[API Request]",
        config.method?.toUpperCase(),
        config.url,
        config.data || ""
      );
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// --- Response Interceptor: Handle Errors & Token Refresh ---
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Logging
    if (process.env.NODE_ENV === "development") {
      console.log(
        "[API Response]",
        response.config.url,
        response.status,
        response.data
      );
    }
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & {
      _retry?: boolean;
    };
    if (!originalRequest || originalRequest._retry) {
      return Promise.reject(error);
    }
    // Handle 401: Try refresh
    if (error.response?.status === 401 && getRefreshToken()) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers && token) {
              originalRequest.headers["Authorization"] = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }
      originalRequest._retry = true;
      isRefreshing = true;
      try {
        const { data } = await axios.post(
          "http://localhost:8080/api/v1/auth/refresh",
          { refreshToken: getRefreshToken() },
          { headers: { "Content-Type": "application/json" } }
        );
        setAccessToken(data.accessToken);
        setRefreshToken(data.refreshToken);
        api.defaults.headers.common[
          "Authorization"
        ] = `Bearer ${data.accessToken}`;
        processQueue(null, data.accessToken);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearTokens();
        // Optionally redirect to login
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    // Handle 403/500
    if (error.response?.status === 403) {
      // Optionally redirect to forbidden page
      console.error("Forbidden: ", error.response.data);
    }
    if (error.response?.status === 500) {
      console.error("Server error: ", error.response.data);
    }
    return Promise.reject(error);
  }
);

// --- Generic API Methods ---
export const get = async <T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> => {
  const { data } = await api.get<T>(url, config);
  return data;
};

export const post = async <T, D = unknown>(
  url: string,
  body: D,
  config?: AxiosRequestConfig
): Promise<T> => {
  const { data } = await api.post<T>(url, body, config);
  return data;
};

export const put = async <T, D = unknown>(
  url: string,
  body: D,
  config?: AxiosRequestConfig
): Promise<T> => {
  const { data } = await api.put<T>(url, body, config);
  return data;
};

export const del = async <T>(
  url: string,
  config?: AxiosRequestConfig
): Promise<T> => {
  const { data } = await api.delete<T>(url, config);
  return data;
};

export default api;
