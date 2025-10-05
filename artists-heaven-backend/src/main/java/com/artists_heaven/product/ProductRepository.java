package com.artists_heaven.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artists_heaven.order.Order;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {

    @Query("SELECT c FROM Category c")
    Set<Category> getAllCategories();

    List<Product> findAll();

    @Query("SELECT p FROM Product p WHERE p.on_Promotion = true")
    List<Product> findAllByOn_Promotion();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> findByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.name")
    Page<Product> findAllProductsSortByName(Pageable pageable);

    @Query(value = "SELECT * FROM Product p where p.available = true ORDER BY p.created_date desc LIMIT 12", nativeQuery = true)
    List<Product> find12ProductsSortedByName();

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.name = :artistName")
    List<Product> findProductsByArtistCategory(@Param("artistName") String artistName);

    @Query("SELECT p FROM Product p WHERE p.section = :section")
    List<Product> findBySection(@Param("section") Section section);

    @Query("SELECT o FROM Order o")
    List<Order> getOrders();

    Product findByNameIgnoreCase(String name);

    @Query("SELECT p FROM Product p LEFT JOIN p.ratings r WHERE p.available = true GROUP BY p.id ORDER BY AVG(r.score) DESC")
    List<Product> findTopRatingProduct();

    boolean existsByReference(Long reference);

    @Query("SELECT p FROM Product p WHERE p.reference = :reference")
    Optional<Product> findByReference(Long reference);

    @Query("SELECT p FROM Product p WHERE p.collection.name = :collectionName")
    List<Product> findByCollectionName(String collectionName);
}
