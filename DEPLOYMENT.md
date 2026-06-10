# Deployment Guide: Railway + Vercel

This project is deployed as two services:

```text
Railway -> Spring Boot backend + Railway MySQL
Vercel  -> React frontend
```

## 1. Push Project To GitHub

From the project root:

```powershell
cd C:\Users\sachi\OneDrive\Desktop\springboot\ecom
git add .
git commit -m "Prepare Railway and Vercel deployment"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

If the remote already exists, use:

```powershell
git remote set-url origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

## 2. Deploy Backend On Railway

1. Open Railway.
2. Create a new project.
3. Add a MySQL database service.
4. Add another service from your GitHub repo.
5. Set the service root directory to:

```text
backend
```

6. Railway will use `backend/Dockerfile` to build and run Spring Boot.
7. In backend service variables, add:

```text
JWT_SECRET=replace-with-a-long-random-secret
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

8. Link/copy these MySQL variables from the Railway MySQL service into the backend service:

```text
MYSQLHOST
MYSQLPORT
MYSQLDATABASE
MYSQLUSER
MYSQLPASSWORD
```

9. Generate a public domain for the backend service.

Example backend URL:

```text
https://stride-ecom-backend.up.railway.app
```

Test:

```text
https://stride-ecom-backend.up.railway.app/api/products
```

## 3. Deploy Frontend On Vercel

1. Open Vercel.
2. Import your GitHub repo.
3. Set the project root directory to:

```text
frontend
```

4. Use these settings:

```text
Framework Preset: Create React App
Build Command: npm run build
Output Directory: build
```

5. Add this environment variable:

```text
REACT_APP_API_URL=https://YOUR_RAILWAY_BACKEND_URL/api
```

Example:

```text
REACT_APP_API_URL=https://stride-ecom-backend.up.railway.app/api
```

6. Deploy the frontend.
7. Copy the Vercel frontend URL.

Example frontend URL:

```text
https://stride-ecom.vercel.app
```

## 4. Update Railway CORS

Go back to Railway backend service variables and update:

```text
CORS_ALLOWED_ORIGINS=https://YOUR_VERCEL_FRONTEND_URL,http://localhost:3000
```

Example:

```text
CORS_ALLOWED_ORIGINS=https://stride-ecom.vercel.app,http://localhost:3000
```

Redeploy/restart the Railway backend after changing this variable.

## 5. Final Test

Open the Vercel URL and test:

1. View products.
2. Register a user.
3. Login.
4. Add product to cart.
5. Place order.
6. Login as admin:

```text
admin@dsatm.edu
admin123
```

7. Add/edit/delete products.
8. View all orders.

## Local Run Note

The backend no longer stores your local MySQL password in GitHub-safe config.
For local IntelliJ runs, add this environment variable in the Run Configuration:

```text
MYSQLPASSWORD=your-local-mysql-password
```

For your current local MySQL setup, use your own password value there.
