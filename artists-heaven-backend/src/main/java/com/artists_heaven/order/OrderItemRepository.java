package com.artists_heaven.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(Long orderId);

}
