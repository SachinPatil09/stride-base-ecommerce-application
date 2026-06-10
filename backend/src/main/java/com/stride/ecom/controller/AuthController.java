package com.stride.ecom.controller;

import com.stride.ecom.dto.*;
import com.stride.ecom.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Public endpoints for register and login
 *
 * POST /api/auth/register  → Create new account
 * POST /api/auth/login     → Login and get JWT token
 *
 * STRIDE: These endpoints use rate limiting (see RateLimitFilter)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register new user
     * Body: { "name": "Sachin", "email": "sachin@email.com", "password": "pass123" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String message = authService.register(req);
        return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
    }

    /**
     * Login and receive JWT token
     * Body: { "email": "sachin@email.com", "password": "pass123" }
     * Response: { "token": "eyJ...", "name": "Sachin", "role": "USER" }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse response = authService.login(req);
        return ResponseEntity.ok(response);
    }
}
