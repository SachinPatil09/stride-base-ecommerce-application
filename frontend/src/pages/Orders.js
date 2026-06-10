import React, { useState, useEffect } from 'react';
import { getMyOrders } from '../api/axios';
import './Orders.css';

const STATUS_COLORS = {
  PENDING:   { color: '#ffd32a', bg: '#ffd32a15' },
  CONFIRMED: { color: '#00d4ff', bg: '#00d4ff15' },
  SHIPPED:   { color: '#a855f7', bg: '#a855f715' },
  DELIVERED: { color: '#00ff88', bg: '#00ff8815' },
  CANCELLED: { color: '#ff4757', bg: '#ff475715' },
};

export default function Orders() {
  const [orders,  setOrders]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);

  useEffect(() => {
    getMyOrders()
      .then(res => setOrders(res.data))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div className="orders-page">
      <h1>📦 My Orders</h1>
      <p className="sub">Your complete order history</p>

      {orders.length === 0 ? (
        <div className="empty">
          <div style={{fontSize:50}}>📦</div>
          <h2>No orders yet</h2>
          <p>Place your first order from the cart!</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map(order => {
            const s = STATUS_COLORS[order.status] || STATUS_COLORS.PENDING;
            const isOpen = expanded === order.id;
            return (
              <div key={order.id} className="order-card">
                {/* Order Header */}
                <div className="order-header" onClick={() => setExpanded(isOpen ? null : order.id)}>
                  <div className="order-meta">
                    <span className="order-id">Order #{order.id}</span>
                    <span className="order-date">
                      {new Date(order.createdAt).toLocaleDateString('en-IN', {
                        day: 'numeric', month: 'short', year: 'numeric'
                      })}
                    </span>
                  </div>
                  <div className="order-right">
                    <span className="order-total">₹{order.totalAmount.toLocaleString()}</span>
                    <span className="order-status" style={{ color: s.color, background: s.bg }}>
                      {order.status}
                    </span>
                    <span className="expand-icon">{isOpen ? '▲' : '▼'}</span>
                  </div>
                </div>

                {/* Order Items (expanded) */}
                {isOpen && (
                  <div className="order-items">
                    {order.items?.map(item => (
                      <div key={item.id} className="order-item">
                        <span className="item-name">{item.product?.name || 'Product'}</span>
                        <span className="item-qty">Qty: {item.quantity}</span>
                        <span className="item-price">₹{item.priceAtOrder?.toLocaleString()}</span>
                        <span className="item-subtotal">
                          ₹{(item.priceAtOrder * item.quantity).toLocaleString()}
                        </span>
                      </div>
                    ))}
                    <div className="order-summary-row">
                      <strong>Total Paid:</strong>
                      <strong style={{color:'#00ff88'}}>₹{order.totalAmount.toLocaleString()}</strong>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
