import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Protects routes that need login
export function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="loading">Loading...</div>;
  if (!user)   return <Navigate to="/login" replace />;
  return children;
}

// Protects routes that need ADMIN role
export function AdminRoute({ children }) {
  const { user, loading, isAdmin } = useAuth();
  if (loading)   return <div className="loading">Loading...</div>;
  if (!user)     return <Navigate to="/login"    replace />;
  if (!isAdmin()) return <Navigate to="/products" replace />;
  return children;
}
