package com.stride.ecom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS Configuration
 * Allows the React frontend (localhost:3000) to call the Spring Boot backend (localhost:8080)
 *
 * STRIDE: Information Disclosure
 * Mitigation: Only allow requests from the known frontend origin — not from any random website
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow requests from our React frontend
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow Authorization header (for JWT) and Content-Type
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies/credentials
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
