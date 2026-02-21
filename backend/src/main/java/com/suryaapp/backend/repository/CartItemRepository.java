package com.suryaapp.backend.repository;

import com.suryaapp.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByAddedAtDesc(Long userId);

    CartItem findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);
}
