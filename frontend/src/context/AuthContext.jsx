import React, { createContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });
  const [token, setToken] = useState(() => localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const res = await authService.getCurrentUser();
          if (res.success && res.data) {
            setUser(res.data);
            localStorage.setItem('user', JSON.stringify(res.data));
          }
        } catch (err) {
          console.error('Failed to restore session:', err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (username, password) => {
    const res = await authService.login({ username, password });
    if (res.success && res.data) {
      const { token: jwtToken, ...userData } = res.data;
      setToken(jwtToken);
      setUser(userData);
      localStorage.setItem('token', jwtToken);
      localStorage.setItem('user', JSON.stringify(userData));
      return userData;
    } else {
      throw new Error(res.message || 'Login failed');
    }
  };

  const register = async (userData) => {
    const res = await authService.register(userData);
    return res;
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const isAdmin = user?.role === 'ADMIN';
  const isOfficer = user?.role === 'INSTITUTION_OFFICER';
  const isComplainant = user?.role === 'COMPLAINANT';
  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAuthenticated,
        isAdmin,
        isOfficer,
        isComplainant,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
