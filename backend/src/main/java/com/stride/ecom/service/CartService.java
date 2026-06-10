package com.stride.ecom.service;

import com.stride.ecom.dto.AddToCartRequest;
import com.stride.ecom.entity.*;
import com.stride.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CartService — manages user's shopping cart
 *
 * STRIDE Notes:
 *  Tampering: Cart belongs to authenticated user — user cannot modify another user's cart
 *  Tampering: Product price is fetched from DB, not from client request
 */
@Service
public class CartService {

    @Autowired private CartRepository     cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository  productRepository;
    @Autowired private UserRepository     userRepository;

    // Get or create cart for user
    public Cart getCart(String email) {
        User user = getUser(email);
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    // Add item to cart
    public Cart addToCart(String email, AddToCartRequest req) {
        Cart    cart    = getCart(email);
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive()) throw new RuntimeException("Product is not available");
        if (product.getStock() < req.getQuantity()) throw new RuntimeException("Insufficient stock");

        // Check if product already in cart — update quantity instead of adding duplicate
        cartItemRepository.findByCartAndProduct(cart, product).ifPresentOrElse(
                existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + req.getQuantity());
                    cartItemRepository.save(existingItem);
                },
                () -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(req.getQuantity());
                    CartItem savedItem = cartItemRepository.save(item);
                    cart.getItems().add(savedItem);
                }
        );

        return cart;
    }

    // Remove item from cart
    public Cart removeFromCart(String email, Long itemId) {
        Cart cart = getCart(email);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security: verify item belongs to this user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized"); // STRIDE: Tampering mitigation
        }

        cartItemRepository.delete(item);
        cart.getItems().removeIf(cartItem -> cartItem.getId().equals(itemId));
        return cart;
    }

    // Clear entire cart
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
