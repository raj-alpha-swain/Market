package com.suryaapp.backend.repository;

import com.suryaapp.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Basic CRUD operations provided by JpaRepository
}
