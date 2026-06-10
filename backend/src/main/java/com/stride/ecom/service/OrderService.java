package com.stride.ecom.service;

import com.stride.ecom.entity.*;
import com.stride.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OrderService — places orders and manages order history
 *
 * STRIDE Notes:
 *  Repudiation:  Every order stores user ID + timestamp — creates audit trail
 *  Tampering:    Total amount calculated server-side from DB prices — client cannot tamper
 *  Info Disclosure: Users can only view their own orders, not others'
 */
@Service
public class OrderService {

    @Autowired private OrderRepository   orderRepository;
    @Autowired private UserRepository    userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartService       cartService;

    /**
     * Place order from user's current cart
     * 1. Get user's cart
     * 2. Validate stock for all items
     * 3. Calculate total from DB prices (not from client)
     * 4. Save order + items
     * 5. Deduct stock
     * 6. Clear cart
     */
    @Transactional
    public Order placeOrder(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCart(email);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock for every item
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + item.getProduct().getName());
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);  // Link to user — STRIDE: Repudiation (audit trail)

        // Calculate total server-side from DB prices — STRIDE: Tampering mitigation
        double total = cart.getTotalPrice();
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PENDING);

        // Add order items (price at time of order is stored)
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(cartItem.getProduct().getPrice()); // DB price
            order.getItems().add(orderItem);

            // Deduct stock
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order
        cartService.clearCart(cart);

        return savedOrder;
    }

    // Get orders for specific user — STRIDE: Info Disclosure (user sees only their orders)
    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ADMIN: Get all orders from all users
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // ADMIN: Update order status
    public Order updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return orderRepository.save(order);
    }
}
