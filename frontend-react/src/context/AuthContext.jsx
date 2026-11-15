import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('civicbuild_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('civicbuild_token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyToken = async () => {
      if (token) {
        try {
          const res = await authApi.getMe();
          if (res.success && res.data) {
            const userData = {
              id: res.data.id,
              username: res.data.username,
              email: res.data.email,
              fullName: res.data.fullName,
              role: res.data.role.name,
              departmentCode: res.data.departmentCode,
            };
            setUser(userData);
            localStorage.setItem('civicbuild_user', JSON.stringify(userData));
          }
        } catch (err) {
          console.error("Session verification failed, logging out:", err);
          logout();
        }
      }
      setLoading(false);
    };

    verifyToken();
  }, [token]);

  const login = async (username, password) => {
    const res = await authApi.login({ username, password });
    if (res.success && res.data) {
      const { token: jwtToken, id, email, fullName, role, departmentCode } = res.data;
      const userData = { id, username: res.data.username, email, fullName, role, departmentCode };
      setToken(jwtToken);
      setUser(userData);
      localStorage.setItem('civicbuild_token', jwtToken);
      localStorage.setItem('civicbuild_user', JSON.stringify(userData));
      return userData;
    }
    throw new Error(res.message || 'Login failed');
  };

  const register = async (formData) => {
    const res = await authApi.register(formData);
    if (res.success && res.data) {
      const { token: jwtToken, id, username, email, fullName, role, departmentCode } = res.data;
      const userData = { id, username, email, fullName, role, departmentCode };
      setToken(jwtToken);
      setUser(userData);
      localStorage.setItem('civicbuild_token', jwtToken);
      localStorage.setItem('civicbuild_user', JSON.stringify(userData));
      return userData;
    }
    throw new Error(res.message || 'Registration failed');
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('civicbuild_token');
    localStorage.removeItem('civicbuild_user');
  };

  return (
    <AuthContext.Provider value={{
      user,
      token,
      isAuthenticated: !!token && !!user,
      role: user?.role,
      loading,
      login,
      register,
      logout,
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
