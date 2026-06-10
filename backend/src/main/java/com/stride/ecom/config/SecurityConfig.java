package com.stride.ecom.config;

import com.stride.ecom.security.JwtFilter;
import com.stride.ecom.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Central security configuration
 *
 * STRIDE Mitigations implemented here:
 *
 * Spoofing:
 *   → JWT filter validates every request token
 *   → BCrypt hashing prevents password theft
 *
 * Elevation of Privilege:
 *   → /api/admin/** routes require ADMIN role
 *   → /api/user/**  routes require USER or ADMIN role
 *   → @PreAuthorize annotations enforce fine-grained access
 *
 * Information Disclosure:
 *   → Stateless sessions — no session tokens on server
 *   → CORS configured to only allow frontend origin
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    @Autowired private JwtFilter             jwtFilter;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    /**
     * BCryptPasswordEncoder: hashes passwords with salt
     * STRIDE Spoofing: plaintext passwords are NEVER stored in DB
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider: ties user loader + password encoder together
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main security filter chain — defines which routes are public vs protected
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(csrf -> csrf.disable())

            // Configure CORS (detailed config in CorsConfig.java)
            .cors(cors -> {})

            // Define route-level access control
            .authorizeHttpRequests(auth -> auth
                // ── Public routes ─────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()        // register, login
                .requestMatchers("/api/products").permitAll()       // view products (no login needed)
                .requestMatchers("/api/products/{id}").permitAll()  // view single product

                // ── Admin only routes ─────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // STRIDE: Elevation of Privilege

                // ── All other routes require authentication ───
                .anyRequest().authenticated()
            )

            // Stateless session — no server-side sessions (STRIDE: Info Disclosure)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Add JWT filter before Spring's default auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
