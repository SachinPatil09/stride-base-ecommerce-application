package com.stride.ecom.dto;

import com.stride.ecom.entity.Order;
import com.stride.ecom.entity.Product;
import com.stride.ecom.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private UserSummary user;
    private Double totalAmount;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct(),
                        item.getQuantity(),
                        item.getPriceAtOrder()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                UserSummary.from(order.getUser()),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }

    @Data
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private User.Role role;

        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole());
        }
    }

    @Data
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Product product;
        private Integer quantity;
        private Double priceAtOrder;
    }
}
