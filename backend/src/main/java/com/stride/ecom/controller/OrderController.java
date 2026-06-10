package com.stride.ecom.controller;

import com.stride.ecom.dto.OrderResponse;
import com.stride.ecom.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OrderController
 *
 * POST /api/orders                   -> place order from cart
 * GET  /api/orders                   -> my order history
 * GET  /api/admin/orders             -> all orders, admin only
 * PUT  /api/admin/orders/{id}/status -> update order status, admin only
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> placeOrder(Authentication auth) {
        return ResponseEntity.ok(OrderResponse.from(orderService.placeOrder(auth.getName())));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getUserOrders(auth.getName()).stream()
                .map(OrderResponse::from)
                .toList());
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders().stream()
                .map(OrderResponse::from)
                .toList());
    }

    @PutMapping("/api/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(OrderResponse.from(orderService.updateStatus(id, body.get("status"))));
    }
}
