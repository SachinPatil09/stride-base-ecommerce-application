# STRIDE Threat Model Report
## Cloud E-Commerce Application
**Subject:** BIC607 — Threat Analysis | **Institution:** DSATM, Bengaluru
**Framework:** STRIDE + DREAD + OWASP Top 10 2025

---

## 1. System Overview

The ShopSecure E-Commerce application is a cloud-native 3-tier web application:

```
[User Browser] ──HTTPS──> [React Frontend :3000]
                                   │
                               REST API calls
                                   │
                         [Spring Boot Backend :8080]
                          ┌────────┴────────┐
                     [MySQL DB]        [JWT Auth]
```

**Components Modeled:**
| Component | Technology | Trust Zone |
|---|---|---|
| User Browser | React.js | Internet (Untrusted) |
| REST API Server | Spring Boot | Application Layer |
| Authentication | JWT + Spring Security | Application Layer |
| Database | MySQL + JPA | Data Layer (Trusted) |
| Admin Dashboard | React.js | Internet (Privileged) |

---

## 2. Data Flow Diagram (DFD)

```
 ┌──────────────┐         HTTPS          ┌─────────────────────┐
 │  User/Admin  │ ──────────────────────> │   React Frontend    │
 │  (Browser)   │ <────────────────────── │   (localhost:3000)  │
 └──────────────┘    HTML/JS/JSON         └──────────┬──────────┘
                                                     │
                                         REST API (JSON over HTTPS)
                                         Authorization: Bearer <JWT>
                                                     │
                                          ┌──────────▼──────────┐
                                          │  Spring Boot API    │
                                          │  (localhost:8080)   │
                                          │  ┌───────────────┐  │
                                          │  │  JWT Filter   │  │
                                          │  │  (validates   │  │
                                          │  │   every req)  │  │
                                          │  └───────────────┘  │
                                          └──────────┬──────────┘
                                                     │
                                              JPA (JDBC)
                                                     │
                                          ┌──────────▼──────────┐
                                          │   MySQL Database    │
                                          │  - users            │
                                          │  - products         │
                                          │  - orders           │
                                          │  - carts            │
                                          └─────────────────────┘

 Trust Boundaries:
 ═══════════════════════════════════════════════════════
 [UNTRUSTED]  User Browser / Internet
 ─────────────────────────────────────────────────────
 [APP LAYER]  Spring Boot Backend (validates all input)
 ─────────────────────────────────────────────────────
 [TRUSTED]    MySQL Database (only accessible by backend)
 ═══════════════════════════════════════════════════════
```

---

## 3. STRIDE Threat Analysis

### S — Spoofing (Pretending to be someone else)

#### Threat S1: Credential Stuffing on Login
| Field | Details |
|---|---|
| **Component** | POST /api/auth/login |
| **Attack** | Attacker uses a database of 1 million leaked email/password pairs and automates login attempts. Since many users reuse passwords, 5-10% of accounts get compromised. |
| **Example** | `for combo in leaked_passwords.txt: curl -X POST /api/auth/login -d {"email":combo.email,"password":combo.pass}` |
| **Impact** | Mass account takeover — attacker can view orders, steal saved addresses, place fraudulent orders |
| **DREAD Score** | Damage:7 Reproducibility:9 Exploitability:8 Affected:8 Discoverability:7 → **Avg: 7.8 HIGH** |
| **Mitigation Implemented** | Spring Security validates credentials. BCrypt password hashing makes cracking individual passwords computationally infeasible. |
| **Additional Mitigation** | Add account lockout after 5 failures. Integrate CAPTCHA. Check passwords against HaveIBeenPwned API. |
| **OWASP** | A07: Identification and Authentication Failures |

#### Threat S2: JWT Token Forgery
| Field | Details |
|---|---|
| **Component** | All authenticated API endpoints |
| **Attack** | If JWT secret is weak (e.g., "secret"), attacker uses hashcat to crack it offline, then forges admin tokens: `{"role":"ADMIN","email":"attacker@evil.com"}` |
| **Example** | `hashcat -a 0 -m 16500 captured.jwt wordlist.txt` then forge token with admin role |
| **Impact** | Full admin access — view all users, delete products, access all orders |
| **DREAD Score** | Damage:10 Reproducibility:6 Exploitability:7 Affected:9 Discoverability:4 → **Avg: 7.2 HIGH** |
| **Mitigation Implemented** | `app.jwt.secret` is a long random 64-character string in application.properties. JwtUtil uses HMAC-SHA256. Any tampered token fails `validateToken()`. |
| **OWASP** | A02: Cryptographic Failures |

#### Threat S3: JWT Algorithm Confusion (alg=none)
| Field | Details |
|---|---|
| **Component** | JwtUtil.validateToken() |
| **Attack** | Attacker takes a valid JWT, changes header `"alg":"HS256"` to `"alg":"none"`, modifies payload to `{"role":"ADMIN"}`, removes signature. Some JWT libraries accept this. |
| **Impact** | Complete authentication bypass — attacker becomes admin without knowing secret |
| **DREAD Score** | Damage:9 Reproducibility:7 Exploitability:6 Affected:9 Discoverability:5 → **Avg: 7.2 HIGH** |
| **Mitigation Implemented** | `jjwt` library with explicit algorithm `HS256` specified. `Jwts.parserBuilder().setSigningKey(key).build()` rejects unsigned tokens. |
| **OWASP** | A02: Cryptographic Failures |

---

### T — Tampering (Modifying data without authorization)

#### Threat T1: Order Price Tampering
| Field | Details |
|---|---|
| **Component** | POST /api/orders (place order) |
| **Attack** | Attacker adds product to cart (price ₹79,999), intercepts the order request using Burp Suite, modifies the total to ₹1. |
| **Example** | Intercept: `{"cartId":5,"total":79999}` → Modify: `{"cartId":5,"total":1}` |
| **Impact** | Attacker buys ₹79,999 phone for ₹1. Direct financial fraud against business. |
| **DREAD Score** | Damage:9 Reproducibility:8 Exploitability:5 Affected:7 Discoverability:4 → **Avg: 6.6 HIGH** |
| **Mitigation Implemented** | `OrderService.placeOrder()` calls `cart.getTotalPrice()` which calculates from `product.getPrice()` fetched from MySQL — **client sends NO price**. Client only sends productId and quantity. |
| **OWASP** | A01: Broken Access Control |

#### Threat T2: Insecure Direct Object Reference (IDOR) on Orders
| Field | Details |
|---|---|
| **Component** | GET /api/orders |
| **Attack** | User A's order is at `/api/orders`. Attacker (User B) queries with User A's ID: `GET /api/orders?userId=1` or `GET /api/orders/1001` to see another user's private orders |
| **Impact** | Attacker sees names, addresses, products, payment info of all users |
| **DREAD Score** | Damage:8 Reproducibility:7 Exploitability:6 Affected:8 Discoverability:5 → **Avg: 6.8 HIGH** |
| **Mitigation Implemented** | `OrderController.myOrders()` uses `auth.getName()` from JWT (email) — **never trusts user-supplied ID**. `OrderService.getUserOrders(email)` fetches only that user's orders from DB. |
| **OWASP** | A01: Broken Access Control |

#### Threat T3: Admin Product Tampering by Regular User
| Field | Details |
|---|---|
| **Component** | POST/PUT/DELETE /api/admin/products |
| **Attack** | Logged-in user discovers admin API routes and sends: `DELETE /api/admin/products/5` with their USER-role JWT token |
| **Impact** | Regular users can delete or modify any product in the store |
| **DREAD Score** | Damage:8 Reproducibility:8 Exploitability:7 Affected:9 Discoverability:6 → **Avg: 7.6 HIGH** |
| **Mitigation Implemented** | `@PreAuthorize("hasRole('ADMIN')")` on all admin controllers. `SecurityConfig` maps `/api/admin/**` to `.hasRole("ADMIN")`. Roles are loaded from DB by `UserDetailsServiceImpl` — cannot be spoofed. |
| **OWASP** | A01: Broken Access Control |

---

### R — Repudiation (Denying you performed an action)

#### Threat R1: User Denies Placing Order
| Field | Details |
|---|---|
| **Component** | Order Management |
| **Attack** | User places order, receives goods, then contacts support claiming they never ordered. No logs exist to prove they did. |
| **Impact** | Business cannot prove order was placed. Financial loss. Legal disputes. |
| **DREAD Score** | Damage:6 Reproducibility:6 Exploitability:4 Affected:7 Discoverability:4 → **Avg: 5.4 MEDIUM** |
| **Mitigation Implemented** | Every order in the `orders` table stores: `user_id` (links to user), `created_at` (exact timestamp), `total_amount` (server-calculated). The `order_items` table stores `price_at_order` for each item — creates a complete, non-repudiable audit trail. |
| **OWASP** | A09: Security Logging and Monitoring Failures |

#### Threat R2: Admin Denies Deleting Product
| Field | Details |
|---|---|
| **Component** | Admin Product Management |
| **Attack** | Admin deletes an important product. Customer complains. Admin claims they didn't do it. No log exists. |
| **Impact** | No forensic evidence. Cannot identify insider threats or mistakes. |
| **DREAD Score** | Damage:6 Reproducibility:6 Exploitability:4 Affected:7 Discoverability:3 → **Avg: 5.2 MEDIUM** |
| **Mitigation Implemented** | Soft delete — products are marked `active=false` (never deleted from DB). DB record is preserved as evidence. |
| **Additional Recommended** | Add Spring Boot audit logging: log admin email, action, target ID, and timestamp to a separate audit_logs table or log file. |
| **OWASP** | A09: Security Logging and Monitoring Failures |

---

### I — Information Disclosure (Exposing private data)

#### Threat I1: Verbose Error Messages Expose Internals
| Field | Details |
|---|---|
| **Component** | All API endpoints (GlobalExceptionHandler) |
| **Attack** | Attacker sends malformed request. Without proper error handling, Spring Boot returns full stack trace including: file paths, class names, database table names, SQL query structure |
| **Example Response** | `java.sql.SQLSyntaxErrorException: Unknown column 'user_id' in 'where clause' at com.stride.ecom.repository.UserRepository.findByEmail...` |
| **Impact** | Attacker learns DB schema, table names, internal package structure — directly useful for SQL injection attacks |
| **DREAD Score** | Damage:6 Reproducibility:9 Exploitability:9 Affected:9 Discoverability:8 → **Avg: 8.2 HIGH** |
| **Mitigation Implemented** | `GlobalExceptionHandler.handleGeneral()` returns only: `{"status":500,"error":"An internal error occurred"}`. Full exception details are logged server-side only. Stack traces never sent to client. |
| **OWASP** | A05: Security Misconfiguration |

#### Threat I2: User Accessing Other Users' Data
| Field | Details |
|---|---|
| **Component** | GET /api/orders, GET /api/cart |
| **Attack** | Authenticated user tries to access another user's cart or orders by manipulating request parameters |
| **Impact** | Privacy violation — user sees other customers' personal information, addresses, purchase history |
| **DREAD Score** | Damage:7 Reproducibility:7 Exploitability:6 Affected:8 Discoverability:5 → **Avg: 6.6 HIGH** |
| **Mitigation Implemented** | All user-scoped endpoints use `auth.getName()` (email from JWT) to identify the user. The user identity comes from the **server-validated JWT token**, not from any request parameter the user can modify. `CartService.removeFromCart()` verifies `item.getCart().getId().equals(cart.getId())` before deletion. |
| **OWASP** | A01: Broken Access Control |

#### Threat I3: Passwords Stored in Plaintext
| Field | Details |
|---|---|
| **Component** | User Registration, MySQL users table |
| **Attack** | If passwords stored as plaintext and DB is breached (SQL injection, backup leak, insider threat), all user passwords are immediately exposed |
| **Impact** | All user accounts compromised on this site AND on every other site where users reused the same password |
| **DREAD Score** | Damage:10 Reproducibility:3 Exploitability:3 Affected:10 Discoverability:3 → **Avg: 5.8 MEDIUM** |
| **Mitigation Implemented** | `AuthService.register()` calls `passwordEncoder.encode(req.getPassword())`. `SecurityConfig` declares `BCryptPasswordEncoder` bean with default strength 10 (2^10 = 1024 iterations). Even if DB is breached, attacker gets only bcrypt hashes — computationally infeasible to reverse. |
| **OWASP** | A02: Cryptographic Failures |

---

### D — Denial of Service (Making the system unavailable)

#### Threat D1: Login Endpoint Brute Force / DDoS
| Field | Details |
|---|---|
| **Component** | POST /api/auth/login |
| **Attack** | Attacker sends 100,000 requests/second to the login endpoint. Server CPU spikes to 100%, legitimate users cannot log in. Also used to brute-force passwords. |
| **Example** | `while true; do curl -X POST /api/auth/login -d '{"email":"admin@dsatm.edu","password":"test"}'; done` |
| **Impact** | Complete application downtime. No user can log in or place orders. Direct revenue loss. |
| **DREAD Score** | Damage:8 Reproducibility:9 Exploitability:9 Affected:9 Discoverability:9 → **Avg: 8.8 HIGH** |
| **Mitigation Implemented** | Bucket4j rate limiting filter (RateLimitFilter) limits to 20 requests/minute per IP on auth endpoints. Exceeding the limit returns HTTP 429 Too Many Requests. |
| **Additional Recommended** | Deploy behind Cloudflare or AWS Shield for network-level DDoS protection. Add Redis-backed distributed rate limiting for multi-instance deployments. |
| **OWASP** | A04: Insecure Design |

#### Threat D2: Large File Upload / Request Body Attack
| Field | Details |
|---|---|
| **Component** | All POST endpoints |
| **Attack** | Attacker sends a 100MB JSON request body to any POST endpoint. Server runs out of memory parsing it. |
| **Impact** | Server out-of-memory crash. Downtime for all users. |
| **DREAD Score** | Damage:7 Reproducibility:8 Exploitability:8 Affected:8 Discoverability:6 → **Avg: 7.4 HIGH** |
| **Mitigation Implemented** | Add to application.properties: `spring.servlet.multipart.max-file-size=5MB` and `spring.servlet.multipart.max-request-size=10MB`. Spring Boot's default max is 1MB for JSON. |
| **OWASP** | A04: Insecure Design |

---

### E — Elevation of Privilege (Gaining unauthorized access)

#### Threat E1: Regular User Accessing Admin APIs
| Field | Details |
|---|---|
| **Component** | /api/admin/** endpoints |
| **Attack** | Regular user discovers `/api/admin/products` route (from frontend source code or API documentation), sends their USER-role JWT token to gain admin functions |
| **Example** | `curl -X DELETE http://localhost:8080/api/admin/products/1 -H "Authorization: Bearer <user_jwt>"` |
| **Impact** | User can delete all products, view all orders and customer data, modify prices |
| **DREAD Score** | Damage:9 Reproducibility:8 Exploitability:7 Affected:9 Discoverability:8 → **Avg: 8.2 HIGH** |
| **Mitigation Implemented** | Two layers of protection: (1) `SecurityConfig`: `.requestMatchers("/api/admin/**").hasRole("ADMIN")` blocks at filter level. (2) `@PreAuthorize("hasRole('ADMIN')")` on each controller method as second check. Role is loaded from MySQL by `UserDetailsServiceImpl` — cannot be spoofed via JWT payload. |
| **OWASP** | A01: Broken Access Control |

#### Threat E2: Self-Promotion to Admin Role
| Field | Details |
|---|---|
| **Component** | User Registration / JWT token |
| **Attack** | User registers with `{"name":"Hacker","email":"h@evil.com","password":"pass","role":"ADMIN"}` hoping the API accepts the role field |
| **Impact** | Attacker gains ADMIN role and full admin access |
| **DREAD Score** | Damage:9 Reproducibility:8 Exploitability:8 Affected:9 Discoverability:7 → **Avg: 8.2 HIGH** |
| **Mitigation Implemented** | `AuthService.register()` hardcodes `user.setRole(User.Role.USER)` regardless of what is in the request body. The `RegisterRequest` DTO has no `role` field — Spring ignores unknown fields by default. Roles can only be changed directly in the database by a DB admin. |
| **OWASP** | A01: Broken Access Control |

---

## 4. DREAD Risk Summary

| # | Threat | STRIDE | Avg Score | Risk |
|---|---|---|---|---|
| T1 | Login Endpoint DDoS | D | 8.8 | 🔴 HIGH |
| E1 | User Accessing Admin APIs | E | 8.2 | 🔴 HIGH |
| E2 | Self-Promotion to Admin | E | 8.2 | 🔴 HIGH |
| I1 | Verbose Error Messages | I | 8.2 | 🔴 HIGH |
| S1 | Credential Stuffing | S | 7.8 | 🔴 HIGH |
| T3 | Admin Tampering by User | T | 7.6 | 🔴 HIGH |
| D2 | Large Request DoS | D | 7.4 | 🔴 HIGH |
| S2 | JWT Token Forgery | S | 7.2 | 🔴 HIGH |
| S3 | JWT alg=none Attack | S | 7.2 | 🔴 HIGH |
| I2 | Cross-User Data Access | I | 6.6 | 🟡 MEDIUM |
| T1 | Order Price Tampering | T | 6.6 | 🟡 MEDIUM |
| T2 | IDOR on Orders | T | 6.8 | 🟡 MEDIUM |
| I3 | Plaintext Passwords | I | 5.8 | 🟡 MEDIUM |
| R1 | Order Repudiation | R | 5.4 | 🟡 MEDIUM |
| R2 | Admin Action Repudiation | R | 5.2 | 🟡 MEDIUM |

---

## 5. OWASP Top 10 2025 Mapping

| OWASP | Category | Threats Addressed |
|---|---|---|
| A01 | Broken Access Control | T2 (IDOR), T3 (Admin API), E1 (Privilege Escalation), E2 (Self-promotion) |
| A02 | Cryptographic Failures | S2 (JWT Forgery), S3 (alg=none), I3 (Password Storage) |
| A03 | Injection | SQL Injection prevented by JPA parameterized queries |
| A04 | Insecure Design | D1 (DDoS/Rate Limiting), D2 (Large Requests) |
| A05 | Security Misconfiguration | I1 (Verbose Errors), CORS configuration |
| A07 | Auth & Identity Failures | S1 (Credential Stuffing), BCrypt hashing |
| A09 | Logging & Monitoring Failures | R1 (Order Audit Trail), R2 (Admin Actions) |

---

## 6. Security Controls Summary

| Control | Implementation | STRIDE Category |
|---|---|---|
| BCrypt Password Hashing | `BCryptPasswordEncoder` (strength 10) | Spoofing |
| JWT Authentication | `jjwt` library, HS256, 24hr expiry | Spoofing |
| Role-Based Access Control | `@PreAuthorize` + `SecurityConfig` | Elevation of Privilege |
| Input Validation | `@Valid`, `@NotBlank`, `@Email`, `@Min` | Tampering |
| Server-Side Price Calculation | `cart.getTotalPrice()` from DB | Tampering |
| Object-Level Authorization | JWT email → DB user → owned resources only | Tampering |
| Audit Trail for Orders | `user_id` + `created_at` on every order | Repudiation |
| Generic Error Messages | `GlobalExceptionHandler` | Information Disclosure |
| CORS Restriction | Allow only `localhost:3000` | Information Disclosure |
| Rate Limiting | Bucket4j — 20 req/min on auth | Denial of Service |
| Stateless Sessions | JWT — no server-side session storage | Information Disclosure |
