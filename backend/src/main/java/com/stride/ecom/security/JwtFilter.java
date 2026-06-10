package com.stride.ecom.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtFilter — runs on every HTTP request
 *
 * What it does:
 * 1. Reads the Authorization header
 * 2. Extracts the JWT token
 * 3. Validates the token
 * 4. Sets the authenticated user in Spring Security context
 *
 * STRIDE: Spoofing — unauthenticated or tampered tokens are rejected here
 * STRIDE: Elevation of Privilege — user role is loaded from DB, not from token
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil            jwtUtil;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        // Step 1: Extract token from header "Authorization: Bearer <token>"
        String token = extractToken(request);

        // Step 2: Validate token and load user
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);

            // Step 3: Load user details from database (role is loaded from DB — STRIDE: Elevation of Privilege)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 4: Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    // Parse "Bearer <token>" from Authorization header
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
