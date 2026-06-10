package com.stride.ecom.service;

import com.stride.ecom.dto.ProductRequest;
import com.stride.ecom.entity.Product;
import com.stride.ecom.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService — CRUD operations for products
 *
 * STRIDE Notes:
 *  Tampering:  Only ADMIN can create/edit/delete (enforced in SecurityConfig + controller)
 *  Tampering:  Price comes from DB, not from client request when placing orders
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all active products (for all users)
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    // Get single product by ID
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Search products by name
    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    // Get products by category
    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    // ADMIN: Create new product
    public Product createProduct(ProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock() != null ? req.getStock() : 0);
        p.setCategory(req.getCategory());
        p.setImageUrl(req.getImageUrl());
        return productRepository.save(p);
    }

    // ADMIN: Update product
    public Product updateProduct(Long id, ProductRequest req) {
        Product p = getProduct(id);
        if (req.getName()        != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getPrice()       != null) p.setPrice(req.getPrice());
        if (req.getStock()       != null) p.setStock(req.getStock());
        if (req.getCategory()    != null) p.setCategory(req.getCategory());
        if (req.getImageUrl()    != null) p.setImageUrl(req.getImageUrl());
        return productRepository.save(p);
    }

    // ADMIN: Soft delete (set active=false, don't delete from DB)
    public void deleteProduct(Long id) {
        Product p = getProduct(id);
        p.setActive(false);
        productRepository.save(p);
    }

    // ADMIN: Get all products including inactive
    public List<Product> getAllProductsAdmin() {
        return productRepository.findAll();
    }
}
