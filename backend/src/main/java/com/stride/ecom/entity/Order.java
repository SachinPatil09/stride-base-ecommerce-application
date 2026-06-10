package com.stride.ecom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity
 * Created when user places an order from their cart.
 *
 * STRIDE Note:
 *  - Repudiation: Every order is timestamped with user ID — creates an audit trail
 *  - Tampering: Total amount is calculated server-side, not trusted from client
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link order to user — creates audit trail (STRIDE: Repudiation mitigation)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Total is calculated on server — client cannot tamper it (STRIDE: Tampering mitigation)
    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // Timestamp for audit trail (STRIDE: Repudiation mitigation)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
