package com.artists_heaven.product;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long>{

    @Query("SELECT c FROM Category c")
    Set<Category> getAllCategories();

    List<Product> findAll();

}
