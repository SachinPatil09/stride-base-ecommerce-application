package com.stride.ecom.controller;

import com.stride.ecom.dto.AddToCartRequest;
import com.stride.ecom.dto.CartResponse;
import com.stride.ecom.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * CartController
 *
 * GET    /api/cart              → view my cart
 * POST   /api/cart/add          → add item to cart
 * DELETE /api/cart/remove/{id}  → remove item from cart
 *
 * All endpoints require authentication (JWT token in header)
 * STRIDE: Tampering — user can only modify their own cart
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // View current user's cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        return ResponseEntity.ok(CartResponse.from(cartService.getCart(auth.getName()))); // auth.getName() = email from JWT
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(Authentication auth,
                                                  @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(CartResponse.from(cartService.addToCart(auth.getName(), req)));
    }

    // Remove item from cart
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(Authentication auth,
                                                       @PathVariable Long itemId) {
        return ResponseEntity.ok(CartResponse.from(cartService.removeFromCart(auth.getName(), itemId)));
    }
}
