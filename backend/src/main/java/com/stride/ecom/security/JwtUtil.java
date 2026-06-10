package com.stride.ecom.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtil — handles JWT token creation and validation
 *
 * STRIDE Threat: Spoofing
 * Mitigation: Tokens are signed with a secret key using HMAC-SHA256.
 *             Any tampered token will fail signature verification.
 *
 * STRIDE Threat: Information Disclosure
 * Mitigation: Token contains only email (not password). Secret key is stored
 *             in application.properties, NOT hardcoded in code.
 */
@Component
public class JwtUtil {

    // Secret key read from application.properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Token validity: 24 hours (read from properties)
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    // Build the signing key from secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate JWT token for a given email
     * Token contains: email as subject, issued time, expiry time
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate token: check signature + expiry
     * Returns false if token is tampered, expired, or invalid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid — reject it (STRIDE: Spoofing mitigation)
            return false;
        }
    }
}
