package com.suryaapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName; // Snapshot in case product is deleted

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // Lock price at time of purchase

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal; // quantity * priceAtPurchase

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (quantity != null && priceAtPurchase != null) {
            subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
