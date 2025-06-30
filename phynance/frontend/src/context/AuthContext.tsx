import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthState {
  user: AuthUser | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

interface AuthContextProps extends AuthState {
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextProps | undefined>(undefined);

export const useAuthContext = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuthContext must be used within AuthProvider");
  return ctx;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const { login: loginApi, logout: logoutApi } = useAuth();
  const navigate = useNavigate();

  // Load user from localStorage on mount
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    const token = localStorage.getItem("access_token");
    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  // Token refresh logic (poll every 5 min)
  useEffect(() => {
    if (!isAuthenticated) return;
    const interval = setInterval(() => {
      refresh();
    }, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, [isAuthenticated]);

  const login = async (username: string, password: string) => {
    setLoading(true);
    setError(null);
    try {
      await loginApi({ username, password });
      // Fetch user profile after login
      const userRes = await fetchUserProfile();
      setUser(userRes);
      setIsAuthenticated(true);
      localStorage.setItem("user", JSON.stringify(userRes));
      setLoading(false);
      navigate("/dashboard");
    } catch (err: any) {
      setError(err.message || "Login failed");
      setLoading(false);
    }
  };

  const logout = () => {
    logoutApi();
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem("user");
    navigate("/login");
  };

  const refresh = async () => {
    // Implement token refresh logic if needed
    // Optionally fetch user profile again
  };

  const fetchUserProfile = async (): Promise<AuthUser> => {
    // Replace with your API call
    const res = await fetch("http://localhost:8080/api/v1/auth/me", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("access_token")}`,
      },
    });
    if (!res.ok) throw new Error("Failed to fetch user profile");
    return res.json();
  };

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated, loading, error, login, logout, refresh }}
    >
      {children}
    </AuthContext.Provider>
  );
};
