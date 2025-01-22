package com.artists_heaven.shoppingCart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem WHERE id = :id")
    void deleteCartItemById(Long id);

    // @Query("SELECT FROM CartItem WHERE ")
    // CartItem getCartItemByUserIdAndShoppingCartId(Long userId, Long ShoppingCartId);

}
