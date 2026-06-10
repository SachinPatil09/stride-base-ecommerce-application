package com.stride.ecom.repository;
import com.stride.ecom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
