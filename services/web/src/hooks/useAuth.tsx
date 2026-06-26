'use client';

/**
 * Auth Context — manages user authentication state.
 *
 * Provides login, register, logout actions.
 * Token is persisted in localStorage via tokenManager.
 * NEVER logs passwords or tokens to the console.
 */

import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { authApi, tokenManager } from '@/lib/api-client';
import type { UserResponse } from '@/lib/types';

interface AuthContextValue {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<string | null>;
  register: (name: string, email: string, password: string) => Promise<string | null>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/** Parse user info from a JWT access_token payload (base64). */
function parseUserFromToken(token: string): UserResponse | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = JSON.parse(atob(parts[1]));
    if (payload.sub && payload.name && payload.email) {
      return {
        id: payload.sub,
        name: payload.name,
        email: payload.email,
        created_at: payload.created_at || '',
      };
    }
    return null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(() => Boolean(tokenManager.getToken()));

  useEffect(() => {
    const token = tokenManager.getToken();
    if (!token) return;

    let cancelled = false;
    const parsed = parseUserFromToken(token);

    authApi.me().then((result) => {
      if (cancelled) return;
      if (result.data) {
        setUser(result.data);
      } else if (parsed) {
        setUser(parsed);
      } else {
        tokenManager.removeToken();
        setUser(null);
      }
      setIsLoading(false);
    });

    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(
    async (email: string, password: string): Promise<string | null> => {
      setIsLoading(true);
      const result = await authApi.login({ email, password });
      setIsLoading(false);

      if (result.error || !result.data) {
        return result.error || 'Đăng nhập thất bại';
      }

      tokenManager.setToken(result.data.access_token);
      setUser(result.data.user);
      return null;
    },
    [],
  );

  const register = useCallback(
    async (name: string, email: string, password: string): Promise<string | null> => {
      setIsLoading(true);
      const result = await authApi.register({ name, email, password });
      setIsLoading(false);

      if (result.error || !result.data) {
        return result.error || 'Đăng ký thất bại';
      }

      tokenManager.setToken(result.data.access_token);
      setUser(result.data.user);
      return null;
    },
    [],
  );

  const logout = useCallback(() => {
    tokenManager.removeToken();
    setUser(null);
  }, []);

  const value: AuthContextValue = {
    user,
    isAuthenticated: user !== null,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
