package com.artists_heaven.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    List<Order> getOrdersByUserId(Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE EXTRACT(YEAR FROM o.createdDate) = :year")
    Integer getNumOrdersPerYear(@Param("year") int year);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE EXTRACT(YEAR FROM o.createdDate) = :year")
    Double getIncomePerYear(@Param("year") int year);

}
