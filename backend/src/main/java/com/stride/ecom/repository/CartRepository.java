package com.stride.ecom.repository;
import com.stride.ecom.entity.Cart;
import com.stride.ecom.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUser(User user);
}
