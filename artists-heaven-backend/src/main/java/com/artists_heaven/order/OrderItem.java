package com.artists_heaven.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String size;
    private Integer quantity;

    public OrderItem() {
    }

    public OrderItem(Long productId, int quantity, String size) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
    }
}
