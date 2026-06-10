-- ============================================================
-- STRIDE E-Commerce Database Schema
-- Subject: BIC607 Threat Analysis | DSATM Bengaluru
-- Run this manually OR let Spring Boot JPA auto-create tables
-- ============================================================

CREATE DATABASE IF NOT EXISTS stride_ecom;
USE stride_ecom;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,   -- Unique constraint prevents duplicate accounts
    password   VARCHAR(255) NOT NULL,          -- BCrypt hashed — NEVER stored in plaintext
    role       ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(10,2) NOT NULL CHECK (price > 0),  -- Price validated server-side
    stock       INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category    VARCHAR(100),
    image_url   VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Carts table (one per user)
CREATE TABLE IF NOT EXISTS carts (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    FOREIGN KEY (cart_id)    REFERENCES carts(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Orders table (audit trail — STRIDE: Repudiation mitigation)
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,                -- Links order to user — audit trail
    total_amount DECIMAL(10,2) NOT NULL,         -- Server-calculated — STRIDE: Tampering mitigation
    status       ENUM('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,  -- Timestamp for audit trail
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items (stores price at time of order)
CREATE TABLE IF NOT EXISTS order_items (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    product_id     BIGINT NOT NULL,
    quantity       INT NOT NULL,
    price_at_order DECIMAL(10,2) NOT NULL,  -- Snapshot of price when order was placed
    FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ── Sample Data ─────────────────────────────────────────────────────
-- Admin user (password: admin123 — BCrypt hash below)
INSERT IGNORE INTO users (name, email, password, role) VALUES
('Admin', 'admin@dsatm.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHuu', 'ADMIN');
-- Note: The hash above is BCrypt of "admin123"
-- DataInitializer.java will also create this on first startup

-- Sample products
INSERT IGNORE INTO products (name, description, price, stock, category) VALUES
('Samsung Galaxy S24',    'Latest Samsung flagship with 200MP camera',    79999, 10, 'Mobiles'),
('Apple iPhone 15',       'iPhone 15 with Dynamic Island and USB-C',       89999,  8, 'Mobiles'),
('Dell Inspiron 15',      '15.6 inch laptop Intel i5 16GB RAM',            55000,  5, 'Laptops'),
('Sony WH-1000XM5',       'Industry-leading noise cancelling headphones',  28990, 20, 'Audio'),
('Logitech MX Master 3',  'Advanced wireless mouse for productivity',       9999, 25, 'Accessories');
