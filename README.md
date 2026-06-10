# 🛒 ShopSecure — Cloud E-Commerce with STRIDE Threat Model

**Subject:** BIC607 — Threat Analysis | **Institution:** DSATM, Bengaluru | **VTU 2022 Scheme**

---

## 🏗️ Project Structure

```
stride-ecom/
├── backend/                          ← Spring Boot (Java 17)
│   ├── src/main/java/com/stride/ecom/
│   │   ├── EcomApplication.java      ← Main entry point
│   │   ├── config/
│   │   │   ├── SecurityConfig.java   ← JWT + RBAC security
│   │   │   ├── CorsConfig.java       ← CORS for React
│   │   │   └── DataInitializer.java  ← Sample data on startup
│   │   ├── controller/
│   │   │   ├── AuthController.java   ← /api/auth/register, /login
│   │   │   ├── ProductController.java← /api/products, /api/admin/products
│   │   │   ├── CartController.java   ← /api/cart
│   │   │   └── OrderController.java  ← /api/orders, /api/admin/orders
│   │   ├── service/                  ← Business logic
│   │   ├── entity/                   ← JPA entities (User, Product, Order...)
│   │   ├── repository/               ← Spring Data JPA repositories
│   │   ├── security/
│   │   │   ├── JwtUtil.java          ← Token generation + validation
│   │   │   ├── JwtFilter.java        ← Runs on every request
│   │   │   └── UserDetailsServiceImpl.java
│   │   ├── dto/                      ← Request/Response data objects
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   ├── src/main/resources/
│   │   └── application.properties    ← DB config, JWT secret
│   └── pom.xml
│
├── frontend/                         ← React.js
│   ├── src/
│   │   ├── api/axios.js              ← All API calls + JWT interceptor
│   │   ├── context/AuthContext.js    ← Global auth state
│   │   ├── components/
│   │   │   ├── Navbar.js             ← Navigation bar
│   │   │   └── ProtectedRoute.js     ← Route guards
│   │   └── pages/
│   │       ├── Login.js              ← Login page
│   │       ├── Register.js           ← Register page
│   │       ├── Products.js           ← Browse products
│   │       ├── Cart.js               ← Shopping cart
│   │       ├── Orders.js             ← Order history
│   │       └── admin/
│   │           ├── AdminProducts.js  ← Manage products (ADMIN)
│   │           └── AdminOrders.js    ← Manage orders (ADMIN)
│   └── package.json
│
└── docs/
    ├── STRIDE_REPORT.md              ← Full threat model analysis
    ├── schema.sql                    ← MySQL database schema
    └── README.md                    ← This file
```

---

## ⚙️ Prerequisites — Install These First

| Tool | Version | Download |
|---|---|---|
| **Java JDK** | 17 or above | https://adoptium.net |
| **Maven** | 3.8+ | https://maven.apache.org |
| **MySQL** | 8.0+ | https://dev.mysql.com/downloads |
| **Node.js** | 18+ | https://nodejs.org |
| **npm** | comes with Node.js | — |

**Verify installations:**
```bash
java  --version    # should show 17.x
mvn   --version    # should show 3.x
mysql --version    # should show 8.x
node  --version    # should show 18.x
npm   --version    # should show 9.x
```

---

## 🚀 Step-by-Step Setup

### Step 1 — Set Up MySQL Database

Open MySQL and run:
```sql
CREATE DATABASE stride_ecom;
```

### Step 2 — Configure Database Connection

Open `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/stride_ecom?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root          ← change to your MySQL username
spring.datasource.password=root          ← change to your MySQL password
```

### Step 3 — Start the Backend

```bash
cd backend
mvn spring-boot:run
```

You should see:
```
✅  STRIDE E-Commerce Backend started!
📋  API Base URL: http://localhost:8080/api
✅ Sample products added to database
✅ Admin user created: admin@dsatm.edu / admin123
```

**Backend is now running on http://localhost:8080**

### Step 4 — Start the Frontend

Open a **new terminal window**:
```bash
cd frontend
npm install
npm start
```

**Frontend is now running on http://localhost:3000**

---

## 🔑 Default Login Credentials

| Role | Email | Password |
|---|---|---|
| **ADMIN** | admin@dsatm.edu | admin123 |
| **USER** | Register a new account at /register | — |

---

## 📋 API Endpoints Reference

### Authentication (Public — No JWT required)
```
POST /api/auth/register    → Register new user
POST /api/auth/login       → Login, returns JWT token
```

### Products (Public)
```
GET  /api/products              → List all active products
GET  /api/products?search=phone → Search products
GET  /api/products?category=Mobiles → Filter by category
GET  /api/products/{id}         → Get single product
```

### Cart (Requires JWT)
```
GET    /api/cart              → View my cart
POST   /api/cart/add          → Add item: {"productId":1,"quantity":2}
DELETE /api/cart/remove/{id}  → Remove cart item
```

### Orders (Requires JWT)
```
POST /api/orders              → Place order from cart
GET  /api/orders              → My order history
```

### Admin (Requires JWT + ADMIN Role)
```
GET    /api/admin/products           → All products
POST   /api/admin/products           → Create product
PUT    /api/admin/products/{id}      → Update product
DELETE /api/admin/products/{id}      → Delete product
GET    /api/admin/orders             → All customer orders
PUT    /api/admin/orders/{id}/status → Update order status
```

---

## 🔐 Security Features (STRIDE Mitigations)

| STRIDE | Threat | Our Mitigation |
|---|---|---|
| **Spoofing** | Fake login | BCrypt password hashing |
| **Spoofing** | Forged JWT | HMAC-SHA256 signed tokens with long random secret |
| **Tampering** | Price manipulation | Server calculates total from DB — client sends no price |
| **Tampering** | Cross-user access | JWT email identity — not trusting client-sent IDs |
| **Tampering** | Unauthorized admin | @PreAuthorize("hasRole('ADMIN')") on all admin endpoints |
| **Repudiation** | Deny placing order | order.user_id + created_at timestamp — full audit trail |
| **Info Disclosure** | Stack traces | GlobalExceptionHandler returns only generic messages |
| **Info Disclosure** | Cross-user data | Users see only their own orders and cart |
| **Denial of Service** | DDoS on login | Bucket4j rate limiting — 20 req/min per IP |
| **Elevation of Privilege** | User → Admin | SecurityConfig blocks /api/admin/** for USER role |
| **Elevation of Privilege** | Self-promote role | Register always sets role=USER, ignores request body |

---

## 🎯 Demo Flow for College Presentation

1. **Register** a new user account at http://localhost:3000/register
2. **Browse** products without logging in
3. **Login** as regular user → add products to cart
4. **Place an order** → see order history
5. **Logout** → login as admin (admin@dsatm.edu / admin123)
6. **Admin panel** → Add a new product, update stock, manage orders
7. Show the **STRIDE Report** (`docs/STRIDE_REPORT.md`) alongside the running app

---

## 🗄️ Database Schema (Auto-created by Spring JPA)

```
users         → id, name, email, password(BCrypt), role, created_at
products      → id, name, description, price, stock, category, active
carts         → id, user_id
cart_items    → id, cart_id, product_id, quantity
orders        → id, user_id, total_amount, status, created_at
order_items   → id, order_id, product_id, quantity, price_at_order
```

---

## 📚 Technologies Used

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | React.js 18 | UI, routing, state |
| HTTP Client | Axios | API calls with JWT interceptor |
| Backend | Spring Boot 3.2 | REST API, business logic |
| Security | Spring Security + JWT (jjwt) | Authentication & authorization |
| ORM | Spring Data JPA + Hibernate | Database operations |
| Database | MySQL 8 | Data persistence |
| Rate Limiting | Bucket4j | DoS protection |
| Password | BCrypt | Secure password hashing |

---

*DSATM Bengaluru | BIC607 Threat Analysis | VTU 2022 Scheme*
