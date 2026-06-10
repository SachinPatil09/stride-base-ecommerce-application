package com.stride.ecom.repository;
import com.stride.ecom.entity.Order;
import com.stride.ecom.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findAllByOrderByCreatedAtDesc();
}
