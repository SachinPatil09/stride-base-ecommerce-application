package com.stride.ecom.dto;

import com.stride.ecom.entity.Cart;
import com.stride.ecom.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private double totalPrice;

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(item.getId(), item.getProduct(), item.getQuantity()))
                .toList();
        return new CartResponse(cart.getId(), items, cart.getTotalPrice());
    }

    @Data
    @AllArgsConstructor
    public static class CartItemResponse {
        private Long id;
        private Product product;
        private Integer quantity;
    }
}
