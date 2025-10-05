package com.artists_heaven.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.artists_heaven.product.Section;

class OrderItemTest {

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

        Order order = new Order();
        order.setId(1L);
        Section section = Section.ACCESSORIES;
        OrderItem orderItem = new OrderItem(productId, quantity, size, name, price, order,section,"colorTest");

        assertNotNull(orderItem);
        assertEquals(productId, orderItem.getProductId());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(size, orderItem.getSize());
        assertEquals(name, orderItem.getName());
        assertEquals(price, orderItem.getPrice());
    }
}