import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logoutUser, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutUser();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <Link to="/">🛒 ShopSecure</Link>
        <span className="nav-badge">STRIDE Protected</span>
      </div>

      <div className="nav-links">
        <Link to="/products" className="nav-link">Products</Link>

        {user ? (
          <>
            <Link to="/cart"   className="nav-link">🛍️ Cart</Link>
            <Link to="/orders" className="nav-link">📦 Orders</Link>

            {/* Admin menu — only shown to ADMIN users */}
            {isAdmin() && (
              <>
                <Link to="/admin/products" className="nav-link admin-link">⚙️ Manage Products</Link>
                <Link to="/admin/orders"   className="nav-link admin-link">📋 Manage Orders</Link>
              </>
            )}

            <span className="nav-user">👤 {user.name}</span>
            <span className={`role-badge ${isAdmin() ? 'admin' : 'user'}`}>
              {user.role}
            </span>
            <button onClick={handleLogout} className="btn-logout">Logout</button>
          </>
        ) : (
          <>
            <Link to="/login"    className="nav-link">Login</Link>
            <Link to="/register" className="btn-primary">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
