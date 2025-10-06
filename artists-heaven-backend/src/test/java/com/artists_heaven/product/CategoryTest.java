package com.artists_heaven.product;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;

class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    @Test
    void testCategoryCreation() {
        category.setId(1L);
        category.setName("Test Category");
        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Test Category", category.getName());
    }

    @Test
    void testCategoryCreation2() {
        Category categoryTest = new Category(1L);
        assertEquals(1L, categoryTest.getId());
    }

    @Test
    void testCategoryWithProducts() {
        Product product1 = new Product();
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setName("Product 2");

        category.setProducts(new HashSet<>());
        category.getProducts().add(product1);
        category.getProducts().add(product2);

        assertEquals(2, category.getProducts().size());
        assertTrue(category.getProducts().contains(product1));
        assertTrue(category.getProducts().contains(product2));
    }
}
