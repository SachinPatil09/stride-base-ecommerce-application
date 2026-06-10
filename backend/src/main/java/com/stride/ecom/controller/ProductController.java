package com.stride.ecom.controller;

import com.stride.ecom.dto.ProductRequest;
import com.stride.ecom.entity.Product;
import com.stride.ecom.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController
 *
 * Public routes (no login needed):
 *   GET /api/products          → list all products
 *   GET /api/products/{id}     → get one product
 *   GET /api/products/search   → search by name
 *
 * Admin only routes:
 *   POST   /api/admin/products       → create product
 *   PUT    /api/admin/products/{id}  → update product
 *   DELETE /api/admin/products/{id}  → delete product
 *
 * STRIDE: Elevation of Privilege — @PreAuthorize("hasRole('ADMIN')") blocks non-admins
 */
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    // ── Public endpoints ──────────────────────────────────────

    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(search));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.getByCategory(category));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    // ── Admin only endpoints ──────────────────────────────────

    // Get ALL products (including inactive) — Admin only
    @GetMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProductsAdmin() {
        return ResponseEntity.ok(productService.getAllProductsAdmin());
    }

    // Create new product — Admin only
    @PostMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.createProduct(req));
    }

    // Update product — Admin only
    @PutMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.updateProduct(id, req));
    }

    // Delete product — Admin only
    @DeleteMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("{\"message\": \"Product deleted\"}");
    }
}
