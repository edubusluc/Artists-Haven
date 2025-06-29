package com.artists_heaven.order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.lastUpdateDateTime asc")
    Page<Order> getOrdersByUserIdPageable(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> getOrdersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE EXTRACT(YEAR FROM o.createdDate) = :year")
    Integer getNumOrdersPerYear(@Param("year") int year);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE EXTRACT(YEAR FROM o.createdDate) = :year")
    Double getIncomePerYear(@Param("year") int year);

    @Query("SELECT o FROM Order o where o.identifier = :identifier")
    Order findOrderByIdentifier(Long identifier);

}
