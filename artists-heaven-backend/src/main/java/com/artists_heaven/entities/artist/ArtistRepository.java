package com.artists_heaven.entities.artist;

import org.springframework.stereotype.Repository;

import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
  Boolean existsByArtistName(String artistName);

  Artist findByEmail(String email);

  @Query("SELECT a.isVerificated FROM Artist a WHERE a.id = :id")
  Boolean isArtistVerificated(@Param("id") Long id);

  @Query("SELECT COUNT(e) FROM Event e WHERE e.artist.id = :id AND EXTRACT(YEAR FROM e.date) = :year")
  Integer findFutureEventsForArtist(Long id, Integer year);

  @Query("SELECT COUNT(e) FROM Event e WHERE e.artist.id = :id AND EXTRACT(YEAR FROM e.date) = :year AND e.date < CURRENT_DATE")
  Integer findPastEventsForArtist(@Param("id") Long id, @Param("year") Integer year);

  @Query("""
          SELECT oi
          FROM OrderItem oi
          JOIN Product p ON oi.productId = p.id
          JOIN p.categories c
          WHERE c.name = :categoryName
            AND EXTRACT(YEAR FROM  oi.order.createdDate) = :year
      """)
  List<OrderItem> getOrdersPerYear(@Param("categoryName") String categoryName, @Param("year") Integer year);

  @Query("SELECT v.status FROM Verification v WHERE v.artist.id = :id ORDER BY v.date DESC")
  List<VerificationStatus> findLatestVerificationStatus(@Param("id") Long id);

  @Query("""
          SELECT
              EXTRACT(MONTH FROM o.createdDate) AS month,
              SUM(oi.quantity) AS totalQuantity
          FROM OrderItem oi
          JOIN Product p ON oi.productId = p.id
          JOIN p.categories c
          JOIN oi.order o
          WHERE c.name = :categoryName
            AND EXTRACT(YEAR FROM o.createdDate) = :year
            AND o.status <> :status
          GROUP BY EXTRACT(MONTH FROM o.createdDate)
          ORDER BY month
      """)
  List<Object[]> findMonthlySalesData(
      @Param("categoryName") String categoryName,
      @Param("year") int year,
      @Param("status") OrderStatus status);

  @Query("SELECT a from Artist a where a.isVerificated = true")
  List<Artist> findValidaAritst();

}
