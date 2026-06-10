import React, { useState, useEffect } from 'react';
import { getProducts, addToCart } from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Products.css';

export default function Products() {
  const [products,  setProducts]  = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [search,    setSearch]    = useState('');
  const [category,  setCategory]  = useState('');
  const [cartMsg,   setCartMsg]   = useState('');
  const [adding,    setAdding]    = useState(null);
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  useEffect(() => { fetchProducts(); }, [search, category]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search)   params.search   = search;
      if (category) params.category = category;
      const res = await getProducts(params);
      setProducts(res.data);
    } catch {
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async (product) => {
    if (!isLoggedIn()) { navigate('/login'); return; }
    setAdding(product.id);
    try {
      await addToCart({ productId: product.id, quantity: 1 });
      setCartMsg(`✅ "${product.name}" added to cart!`);
      setTimeout(() => setCartMsg(''), 3000);
    } catch (err) {
      setCartMsg(`❌ ${err.response?.data?.error || 'Failed to add to cart'}`);
      setTimeout(() => setCartMsg(''), 3000);
    } finally {
      setAdding(null);
    }
  };

  const categories = ['', 'Mobiles', 'Laptops', 'Audio', 'Monitors', 'Accessories', 'Networking'];

  return (
    <div className="products-page">
      <div className="products-header">
        <h1>🛍️ Products</h1>
        <p>Browse our collection of tech products</p>
      </div>

      {/* Search & Filter Bar */}
      <div className="filter-bar">
        <input
          type="text"
          placeholder="🔍  Search products..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)} className="category-select">
          {categories.map(c => (
            <option key={c} value={c}>{c || 'All Categories'}</option>
          ))}
        </select>
      </div>

      {/* Cart notification */}
      {cartMsg && <div className={`cart-toast ${cartMsg.startsWith('✅') ? 'success' : 'error'}`}>{cartMsg}</div>}

      {/* Products Grid */}
      {loading ? (
        <div className="loading">Loading products...</div>
      ) : products.length === 0 ? (
        <div className="empty">No products found</div>
      ) : (
        <div className="products-grid">
          {products.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-img">
                <img src={product.imageUrl || 'https://via.placeholder.com/300x200?text=Product'} alt={product.name} />
              </div>
              <div className="product-body">
                <div className="product-category">{product.category}</div>
                <h3 className="product-name">{product.name}</h3>
                <p className="product-desc">{product.description}</p>
                <div className="product-footer">
                  <div>
                    <div className="product-price">₹{product.price.toLocaleString()}</div>
                    <div className={`product-stock ${product.stock < 5 ? 'low' : ''}`}>
                      {product.stock > 0 ? `${product.stock} in stock` : '❌ Out of stock'}
                    </div>
                  </div>
                  <button
                    onClick={() => handleAddToCart(product)}
                    disabled={product.stock === 0 || adding === product.id}
                    className="btn-add-cart"
                  >
                    {adding === product.id ? '...' : '🛒 Add'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
