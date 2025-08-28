package com.artists_heaven.userProduct;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Long> {
    Long countByOwnerId(Long ownerId);

    @Query("SELECT u FROM UserProduct u WHERE u.status = PENDING")
    List<UserProduct> getAllPendingVerification();

    @Query("SELECT u FROM UserProduct u WHERE u.status = ACCEPTED AND u.createdAt >= :oneMonthAgo")
    List<UserProduct> findAllByMonthAndAccepted(@Param("oneMonthAgo") Date oneMonthAgo);

    Long countByOwnerIdAndCreatedAtBetween(Long ownerId, Date start, Date end);

    List<UserProduct> findByOwnerIdOrderByCreatedAtDesc(Long userId);
}
