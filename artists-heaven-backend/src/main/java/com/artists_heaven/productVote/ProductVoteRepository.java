package com.artists_heaven.productVote;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.userProduct.UserProduct;

public interface ProductVoteRepository extends JpaRepository<ProductVote, Long> {
    boolean existsByUserAndProduct(User user, UserProduct product);

    @Query("SELECT v.product.id, COUNT(v) FROM ProductVote v WHERE v.product.id IN :productIds GROUP BY v.product.id")
    List<Object[]> countVotesForProducts(@Param("productIds") List<Long> productIds);

    @Query("SELECT v.product.id FROM ProductVote v WHERE v.user.id = :userId")
    Set<Long> findProductIdsByUserId(@Param("userId") Long userId);
}
