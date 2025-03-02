package com.artists_heaven.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class OrderItemTest {

    @Test
    void testDefaultConstructor() {
        OrderItem orderItem = new OrderItem();
        assertNotNull(orderItem);
    }

    @Test
    void testParameterizedConstructor() {
        Long productId = 1L;
        int quantity = 2;
        String size = "M";
        String name = "Product Name";
        Float price = 10.0f;

        OrderItem orderItem = new OrderItem(productId, quantity, size, name, price);

        assertNotNull(orderItem);
        assertEquals(productId, orderItem.getProductId());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(size, orderItem.getSize());
        assertEquals(name, orderItem.getName());
        assertEquals(price, orderItem.getPrice());
    }
}