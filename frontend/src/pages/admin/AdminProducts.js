import React, { useState, useEffect } from 'react';
import { getAdminProducts, createProduct, updateProduct, deleteProduct } from '../../api/axios';
import './Admin.css';

const EMPTY_FORM = { name: '', description: '', price: '', stock: '', category: '', imageUrl: '' };
const CATEGORIES = ['Mobiles', 'Laptops', 'Audio', 'Monitors', 'Accessories', 'Networking', 'Other'];

export default function AdminProducts() {
  const [products, setProducts]   = useState([]);
  const [loading,  setLoading]    = useState(true);
  const [showForm, setShowForm]   = useState(false);
  const [editId,   setEditId]     = useState(null);
  const [form,     setForm]       = useState(EMPTY_FORM);
  const [saving,   setSaving]     = useState(false);
  const [message,  setMessage]    = useState('');

  useEffect(() => { fetchProducts(); }, []);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await getAdminProducts();
      setProducts(res.data);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  };

  const openCreate = () => {
    setForm(EMPTY_FORM);
    setEditId(null);
    setShowForm(true);
  };

  const openEdit = (p) => {
    setForm({
      name: p.name, description: p.description || '',
      price: p.price, stock: p.stock,
      category: p.category || '', imageUrl: p.imageUrl || '',
    });
    setEditId(p.id);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = { ...form, price: parseFloat(form.price), stock: parseInt(form.stock) };
      if (editId) {
        await updateProduct(editId, payload);
        setMessage('✅ Product updated!');
      } else {
        await createProduct(payload);
        setMessage('✅ Product created!');
      }
      setShowForm(false);
      fetchProducts();
    } catch (err) {
      const errData = err.response?.data;
      if (errData?.errors) setMessage('❌ ' + Object.values(errData.errors).join(', '));
      else setMessage('❌ ' + (errData?.error || 'Failed'));
    } finally {
      setSaving(false);
      setTimeout(() => setMessage(''), 3000);
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete "${name}"?`)) return;
    try {
      await deleteProduct(id);
      setMessage('✅ Product deleted');
      fetchProducts();
    } catch { setMessage('❌ Delete failed'); }
    setTimeout(() => setMessage(''), 3000);
  };

  return (
    <div className="admin-page">
      <div className="admin-header">
        <div>
          <h1>⚙️ Manage Products</h1>
          <p>Add, edit, or delete products in the store</p>
        </div>
        <button onClick={openCreate} className="btn-create">+ Add Product</button>
      </div>

      {message && (
        <div className={`alert ${message.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>
          {message}
        </div>
      )}

      {/* Create / Edit Form */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editId ? 'Edit Product' : 'Add New Product'}</h2>
              <button onClick={() => setShowForm(false)} className="btn-close">✕</button>
            </div>
            <form onSubmit={handleSubmit} className="product-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Product Name *</label>
                  <input value={form.name} onChange={e => setForm({...form, name: e.target.value})}
                    placeholder="e.g. Samsung Galaxy S24" required />
                </div>
                <div className="form-group">
                  <label>Category</label>
                  <select value={form.category} onChange={e => setForm({...form, category: e.target.value})}>
                    <option value="">Select category</option>
                    {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})}
                  placeholder="Product description..." rows={3} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Price (₹) *</label>
                  <input type="number" value={form.price} min="0.01" step="0.01"
                    onChange={e => setForm({...form, price: e.target.value})} required />
                </div>
                <div className="form-group">
                  <label>Stock *</label>
                  <input type="number" value={form.stock} min="0"
                    onChange={e => setForm({...form, stock: e.target.value})} required />
                </div>
              </div>
              <div className="form-group">
                <label>Image URL</label>
                <input value={form.imageUrl} onChange={e => setForm({...form, imageUrl: e.target.value})}
                  placeholder="https://..." />
              </div>
              <div className="form-actions">
                <button type="button" onClick={() => setShowForm(false)} className="btn-cancel">Cancel</button>
                <button type="submit" disabled={saving} className="btn-save">
                  {saving ? 'Saving...' : (editId ? 'Update Product' : 'Create Product')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Products Table */}
      {loading ? <div className="loading">Loading...</div> : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th><th>Product</th><th>Category</th>
                <th>Price</th><th>Stock</th><th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td className="muted">#{p.id}</td>
                  <td>
                    <div className="prod-cell">
                      <img src={p.imageUrl || 'https://via.placeholder.com/40x40'} alt={p.name} className="prod-thumb" />
                      <div>
                        <div className="prod-name">{p.name}</div>
                        <div className="prod-desc">{p.description?.substring(0, 40)}...</div>
                      </div>
                    </div>
                  </td>
                  <td className="muted">{p.category || '—'}</td>
                  <td className="price">₹{p.price?.toLocaleString()}</td>
                  <td>
                    <span className={`stock-badge ${p.stock < 5 ? 'low' : ''}`}>{p.stock}</span>
                  </td>
                  <td>
                    <span className={`status-badge ${p.active ? 'active' : 'inactive'}`}>
                      {p.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div className="action-btns">
                      <button onClick={() => openEdit(p)} className="btn-edit">Edit</button>
                      <button onClick={() => handleDelete(p.id, p.name)} className="btn-del">Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
