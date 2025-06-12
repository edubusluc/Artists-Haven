package com.artists_heaven.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderStatus;

@Repository
public interface AdminRepository extends JpaRepository<User, Long> {

        @Query("SELECT COUNT(*) FROM User WHERE role != 'ADMIN'")
        int countUsers();

        @Query("SELECT COUNT(*) FROM User WHERE role = 'ARTIST'")
        Integer countArtist();

        @Query("SELECT " +
                        " EXTRACT(MONTH FROM o.createdDate), COUNT(o), SUM(o.totalPrice) " +
                        "FROM Order o " +
                        "WHERE o.status = :status " +
                        "AND EXTRACT(YEAR FROM o.createdDate) = :year " +
                        "GROUP BY EXTRACT(MONTH FROM o.createdDate) " +
                        "ORDER BY EXTRACT(MONTH FROM o.createdDate)")
        List<Object[]> findMonthlySalesData(@Param("year") int year, @Param("status") OrderStatus status);

        @Query("SELECT o.status, COUNT(o) " +
                        "FROM Order o " +
                        "WHERE EXTRACT(YEAR FROM o.createdDate) = :year " +
                        "GROUP BY o.status")
        List<Object[]> findOrderStatusCounts(int year);

        @Query("SELECT v.status, COUNT(v) " +
                        "FROM Verification v " +
                        "WHERE EXTRACT(YEAR FROM v.date) = :year " +
                        "GROUP BY v.status")
        List<Object[]> findVerificationStatusCounts(int year);

        @Query("SELECT o FROM Order o WHERE EXTRACT(YEAR FROM o.createdDate) = :year")
        List<Order> getOrdersPerYear(@Param("year") int year);

        @Query("SELECT COUNT(p) FROM Product p WHERE p.available = false")
        Integer findNotAvailableProducts();

        @Query("SELECT COUNT(p) FROM Product p WHERE p.available = true")
        Integer findAvailableProducts();

        @Query("SELECT COUNT(p) FROM Product p WHERE p.on_Promotion = true")
        Integer findPromotedProducts();

        @Query("SELECT COUNT(p) FROM Product p")
        Integer findTotalProductsCount();

        @Query("SELECT u FROM User u " +
                        "WHERE (:search IS NULL OR :search = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))) "
                        +
                        "ORDER BY u.role")
        Page<User> findAllSort(@Param("search") String search, Pageable pageable);

}
