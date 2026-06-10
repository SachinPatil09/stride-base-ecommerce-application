package com.stride.ecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**


 * This is the main entry point of the Spring Boot application.
 * Run this class to start the backend server on http://localhost:8080
 */
@SpringBootApplication
public class EcomApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcomApplication.class, args);
        System.out.println("\n  STRIDE E-Commerce Backend started!");
        System.out.println("  API Base URL: http://localhost:8080/api");
        System.out.println("  Auth APIs:   http://localhost:8080/api/auth/register | /login");
        System.out.println("  Products:    http://localhost:8080/api/products\n");
    }
}
