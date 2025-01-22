package com.artists_heaven.shoppingCart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    
    @Query("SELECT s FROM ShoppingCart s WHERE s.user.id = :id")
    Optional <ShoppingCart> findShoppingCartByUserId(long id);

}
