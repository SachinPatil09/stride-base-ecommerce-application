import React, { useState, useEffect } from 'react';
import { getAdminOrders, updateOrderStatus } from '../../api/axios';
import './Admin.css';

const STATUSES = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
const STATUS_COLORS = {
  PENDING:   '#ffd32a', CONFIRMED: '#00d4ff',
  SHIPPED:   '#a855f7', DELIVERED: '#00ff88', CANCELLED: '#ff4757',
};

export default function AdminOrders() {
  const [orders,  setOrders]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [expanded, setExpanded] = useState(null);

  useEffect(() => { fetchOrders(); }, []);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await getAdminOrders();
      setOrders(res.data);
    } catch { setOrders([]); }
    finally { setLoading(false); }
  };

  const handleStatusChange = async (orderId, status) => {
    try {
      await updateOrderStatus(orderId, status);
      setMessage(`✅ Order #${orderId} updated to ${status}`);
      fetchOrders();
    } catch { setMessage('❌ Failed to update status'); }
    setTimeout(() => setMessage(''), 3000);
  };

  const totalRevenue = orders
    .filter(o => o.status === 'DELIVERED')
    .reduce((sum, o) => sum + o.totalAmount, 0);

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>📋 Manage Orders</h1>
          <p>View and update all customer orders</p>
        </div>
      </div>

      {/* Stats Row */}
      <div className="admin-stats">
        {[
          { label: 'Total Orders',    val: orders.length,                                               color: '#00d4ff' },
          { label: 'Pending',         val: orders.filter(o=>o.status==='PENDING').length,               color: '#ffd32a' },
          { label: 'Delivered',       val: orders.filter(o=>o.status==='DELIVERED').length,             color: '#00ff88' },
          { label: 'Revenue (₹)',     val: totalRevenue.toLocaleString(),                               color: '#a855f7' },
        ].map(s => (
          <div key={s.label} className="admin-stat-card">
            <div className="admin-stat-val" style={{color: s.color}}>{s.val}</div>
            <div className="admin-stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {message && (
        <div className={`alert ${message.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>
          {message}
        </div>
      )}

      {loading ? <div className="loading">Loading orders...</div> : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order ID</th><th>Customer</th><th>Date</th>
                <th>Items</th><th>Total</th><th>Status</th><th>Update Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <React.Fragment key={order.id}>
                  <tr onClick={() => setExpanded(expanded === order.id ? null : order.id)}
                    style={{cursor:'pointer'}}>
                    <td className="muted">#{order.id}</td>
                    <td>
                      <div className="prod-name">{order.user?.name}</div>
                      <div className="prod-desc">{order.user?.email}</div>
                    </td>
                    <td className="muted">
                      {new Date(order.createdAt).toLocaleDateString('en-IN',
                        {day:'numeric', month:'short', year:'numeric'})}
                    </td>
                    <td className="muted">{order.items?.length || 0} items</td>
                    <td className="price">₹{order.totalAmount?.toLocaleString()}</td>
                    <td>
                      <span className="status-text" style={{color: STATUS_COLORS[order.status] || '#94a3b8'}}>
                        ● {order.status}
                      </span>
                    </td>
                    <td onClick={e => e.stopPropagation()}>
                      <select
                        value={order.status}
                        onChange={e => handleStatusChange(order.id, e.target.value)}
                        className="status-select"
                      >
                        {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                    </td>
                  </tr>
                  {expanded === order.id && (
                    <tr className="expand-tr">
                      <td colSpan={7}>
                        <div className="order-detail">
                          <strong>Order Items:</strong>
                          {order.items?.map(item => (
                            <div key={item.id} className="order-detail-item">
                              <span>{item.product?.name}</span>
                              <span>Qty: {item.quantity}</span>
                              <span>₹{item.priceAtOrder?.toLocaleString()} each</span>
                              <span style={{color:'#00ff88'}}>
                                ₹{(item.priceAtOrder * item.quantity).toLocaleString()}
                              </span>
                            </div>
                          ))}
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
