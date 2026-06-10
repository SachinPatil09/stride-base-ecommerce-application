package com.stride.ecom.service;

import com.stride.ecom.dto.*;
import com.stride.ecom.entity.User;
import com.stride.ecom.repository.UserRepository;
import com.stride.ecom.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — handles user registration and login
 *
 * STRIDE Mitigations:
 *  Spoofing:   Passwords are BCrypt hashed before saving. JWT token returned on login.
 *  Tampering:  Email uniqueness enforced — cannot register with duplicate email.
 */
@Service
public class AuthService {

    @Autowired private UserRepository        userRepository;
    @Autowired private PasswordEncoder       passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil               jwtUtil;

    /**
     * Register a new user
     * 1. Check if email already exists
     * 2. Hash the password using BCrypt
     * 3. Save user to MySQL
     */
    public String register(RegisterRequest req) {
        // Check for duplicate email
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        // BCrypt hash — STRIDE: Spoofing mitigation (plaintext never stored)
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.USER); // New users always start as USER role

        userRepository.save(user);
        return "Registration successful";
    }

    /**
     * Login: verify credentials, return JWT token
     * 1. Spring Security verifies email + password against DB
     * 2. On success, generate JWT token
     * 3. Return token + user info
     */
    public LoginResponse login(LoginRequest req) {
        // Spring Security handles credential verification
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // Credentials verified — generate JWT token
        String token = jwtUtil.generateToken(req.getEmail());

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
