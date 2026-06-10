import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, AdminRoute } from './components/ProtectedRoute';
import Navbar        from './components/Navbar';
import Login         from './pages/Login';
import Register      from './pages/Register';
import Products      from './pages/Products';
import Cart          from './pages/Cart';
import Orders        from './pages/Orders';
import AdminProducts from './pages/admin/AdminProducts';
import AdminOrders   from './pages/admin/AdminOrders';
import './App.css';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          {/* Public */}
          <Route path="/"         element={<Navigate to="/products" replace />} />
          <Route path="/login"    element={<Login    />} />
          <Route path="/register" element={<Register />} />
          <Route path="/products" element={<Products />} />

          {/* Logged-in users */}
          <Route path="/cart"   element={<ProtectedRoute><Cart   /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><Orders /></ProtectedRoute>} />

          {/* Admin only */}
          <Route path="/admin/products" element={<AdminRoute><AdminProducts /></AdminRoute>} />
          <Route path="/admin/orders"   element={<AdminRoute><AdminOrders   /></AdminRoute>} />

          {/* 404 */}
          <Route path="*" element={<div style={{textAlign:'center',padding:60,color:'#64748b'}}>404 — Page not found</div>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
