package com.stride.ecom.config;

import com.stride.ecom.entity.*;
import com.stride.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer
 * Runs on startup — creates sample products and an admin user if DB is empty.
 * Remove this class in production after first run.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository    userRepository;
    @Autowired private PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if DB is empty
        if (productRepository.count() == 0) {
            seedProducts();
            System.out.println(" Sample products added to database");
        }

        if (userRepository.count() == 0) {
            seedAdminUser();
            System.out.println(" Admin user created: admin@dsatm.edu / admin123");
        }
    }

    private void seedProducts() {
        String[][] products = {
            {"Samsung Galaxy S24",        "Latest Samsung flagship with 200MP camera",              "79999", "10", "Mobiles",    "https://via.placeholder.com/300x300?text=Galaxy+S24"},
            {"Apple iPhone 15",           "iPhone 15 with Dynamic Island and USB-C",                "89999", "8",  "Mobiles",    "https://via.placeholder.com/300x300?text=iPhone+15"},
            {"OnePlus 12",                "Snapdragon 8 Gen 3 flagship from OnePlus",               "64999", "15", "Mobiles",    "https://via.placeholder.com/300x300?text=OnePlus+12"},
            {"Dell Inspiron 15",          "15.6 inch laptop with Intel i5 and 16GB RAM",            "55000", "5",  "Laptops",    "https://via.placeholder.com/300x300?text=Dell+Inspiron"},
            {"HP Pavilion x360",          "2-in-1 touchscreen laptop with 12th Gen Intel",          "62000", "7",  "Laptops",    "https://via.placeholder.com/300x300?text=HP+Pavilion"},
            {"Sony WH-1000XM5",           "Industry-leading noise cancelling headphones",            "28990", "20", "Audio",      "https://via.placeholder.com/300x300?text=Sony+WH1000XM5"},
            {"boAt Rockerz 450",          "Wireless bluetooth headphones with 15hr battery",         "1499",  "50", "Audio",      "https://via.placeholder.com/300x300?text=boAt+450"},
            {"Samsung 27\" Monitor",      "Full HD IPS panel 75Hz gaming monitor",                   "18999", "12", "Monitors",   "https://via.placeholder.com/300x300?text=Samsung+Monitor"},
            {"Logitech MX Master 3",      "Advanced wireless mouse for productivity",                "9999",  "25", "Accessories","https://via.placeholder.com/300x300?text=MX+Master+3"},
            {"Mechanical Keyboard RGB",   "TKL mechanical gaming keyboard with RGB backlight",       "3499",  "30", "Accessories","https://via.placeholder.com/300x300?text=Keyboard+RGB"},
            {"Realme Buds Air 5",         "Active noise cancellation TWS earbuds",                   "2999",  "40", "Audio",      "https://via.placeholder.com/300x300?text=Realme+Buds"},
            {"TP-Link Wi-Fi Router",      "AX1800 Dual Band Wi-Fi 6 Router",                        "3299",  "18", "Networking", "https://via.placeholder.com/300x300?text=TP-Link+Router"},
        };

        for (String[] p : products) {
            Product product = new Product();
            product.setName(p[0]);
            product.setDescription(p[1]);
            product.setPrice(Double.parseDouble(p[2]));
            product.setStock(Integer.parseInt(p[3]));
            product.setCategory(p[4]);
            product.setImageUrl(p[5]);
            productRepository.save(product);
        }
    }

    private void seedAdminUser() {
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@dsatm.edu");
        admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt hashed
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
    }
}
