import React, { useState, useEffect } from 'react';
import { getCart, removeFromCart, placeOrder } from '../api/axios';
import { useNavigate } from 'react-router-dom';
import './Cart.css';

export default function Cart() {
  const [cart,    setCart]    = useState(null);
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  useEffect(() => { fetchCart(); }, []);

  const fetchCart = async () => {
    setLoading(true);
    try {
      const res = await getCart();
      setCart(res.data);
    } catch { setCart(null); }
    finally { setLoading(false); }
  };

  const handleRemove = async (itemId) => {
    try {
      const res = await removeFromCart(itemId);
      setCart(res.data);
    } catch (err) {
      setMessage('Failed to remove item');
    }
  };

  const handlePlaceOrder = async () => {
    setPlacing(true);
    try {
      await placeOrder();
      setMessage('✅ Order placed successfully!');
      await fetchCart();
      setTimeout(() => navigate('/orders'), 2000);
    } catch (err) {
      setMessage(`❌ ${err.response?.data?.error || 'Failed to place order'}`);
    } finally {
      setPlacing(false);
    }
  };

  const total = cart?.items?.reduce(
    (sum, item) => sum + item.product.price * item.quantity, 0
  ) || 0;

  if (loading) return <div className="loading">Loading cart...</div>;

  return (
    <div className="cart-page">
      <h1>🛍️ My Cart</h1>

      {message && (
        <div className={`alert ${message.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>
          {message}
        </div>
      )}

      {!cart?.items?.length ? (
        <div className="cart-empty">
          <div style={{fontSize:60}}>🛒</div>
          <h2>Your cart is empty</h2>
          <button onClick={() => navigate('/products')} className="btn-shop">Browse Products</button>
        </div>
      ) : (
        <div className="cart-layout">
          {/* Cart Items */}
          <div className="cart-items">
            {cart.items.map(item => (
              <div key={item.id} className="cart-item">
                <img src={item.product.imageUrl || 'https://via.placeholder.com/80x80'} alt={item.product.name} className="cart-item-img" />
                <div className="cart-item-info">
                  <div className="cart-item-name">{item.product.name}</div>
                  <div className="cart-item-price">₹{item.product.price.toLocaleString()} × {item.quantity}</div>
                </div>
                <div className="cart-item-total">₹{(item.product.price * item.quantity).toLocaleString()}</div>
                <button onClick={() => handleRemove(item.id)} className="btn-remove">✕</button>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="cart-summary">
            <h2>Order Summary</h2>
            <div className="summary-row"><span>Items ({cart.items.length})</span><span>₹{total.toLocaleString()}</span></div>
            <div className="summary-row"><span>Shipping</span><span style={{color:'#00ff88'}}>FREE</span></div>
            <div className="summary-divider"/>
            <div className="summary-total"><span>Total</span><span>₹{total.toLocaleString()}</span></div>

            <button onClick={handlePlaceOrder} disabled={placing} className="btn-place-order">
              {placing ? 'Placing Order...' : '✅ Place Order'}
            </button>

            {/* STRIDE Note — for demo */}
            <div className="stride-note">
              🔒 <strong>STRIDE Security:</strong> Price calculated server-side from database. Client cannot tamper with order total.
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
