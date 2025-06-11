package com.artists_heaven.product;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT c FROM Category c")
    Set<Category> getAllCategories();

    List<Product> findAll();

    @Query("SELECT p FROM Product p WHERE p.on_Promotion = true")
    List<Product> findAllByOn_Promotion();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> findByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.name")
    Page<Product> findAllProductsSortByName(Pageable pageable);

}
