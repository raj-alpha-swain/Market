package com.suryaapp.backend.controller;

import com.suryaapp.backend.entity.CartItem;
import com.suryaapp.backend.entity.Product;
import com.suryaapp.backend.entity.User;
import com.suryaapp.backend.repository.CartItemRepository;
import com.suryaapp.backend.repository.ProductRepository;
import com.suryaapp.backend.repository.UserRepository;
import com.suryaapp.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // Get user's cart
    @GetMapping
    public ResponseEntity<?> getUserCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsername(authHeader);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedAtDesc(user.getId());
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add item to cart
    @PostMapping
    public ResponseEntity<?> addToCart(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsername(authHeader);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Check if item already in cart
            CartItem existingItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                CartItem updated = cartItemRepository.save(existingItem);
                return ResponseEntity.ok(updated);
            }

            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            CartItem saved = cartItemRepository.save(cartItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update cart item quantity
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            Integer quantity = request.get("quantity");
            if (quantity != null && quantity > 0) {
                cartItem.setQuantity(quantity);
                CartItem updated = cartItemRepository.save(cartItem);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid quantity"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Remove item from cart
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            cartItemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Clear all cart items for user
    @DeleteMapping
    public ResponseEntity<?> clearCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsername(authHeader);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            cartItemRepository.deleteByUserId(user.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractUsername(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUsername(token);
        }
        return "buyer"; // Default for testing
    }
}
