package com.stride.ecom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Product Entity
 * Represents items available in the e-commerce store.
 *
 * STRIDE Note:
 *  - Tampering: Only ADMIN role can create/edit/delete products
 *  - Input validation prevents injection attacks
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    private String description;

    // Price validated server-side — client CANNOT tamper with it (STRIDE: Tampering)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock = 0;

    @Size(max = 100)
    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
