import React, { createContext, useContext, useState, useEffect } from 'react';

// Create context
const AuthContext = createContext(null);

// AuthProvider wraps the whole app and provides auth state everywhere
export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(null);
  const [loading, setLoading] = useState(true);

  // On page refresh — restore user from localStorage
  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      setUser(JSON.parse(userData));
    }
    setLoading(false);
  }, []);

  // Called after successful login
  const loginUser = (userData, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  // Called on logout
  const logoutUser = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const isAdmin = () => user?.role === 'ADMIN';
  const isLoggedIn = () => !!user;

  return (
    <AuthContext.Provider value={{ user, loading, loginUser, logoutUser, isAdmin, isLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook — use this in any component to access auth
export function useAuth() {
  return useContext(AuthContext);
}
