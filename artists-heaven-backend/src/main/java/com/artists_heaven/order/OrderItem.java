package com.artists_heaven.order;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
    private String name;
    private Long productId;
    private String size;
    private Integer quantity;
    private Float price;

    public OrderItem() {
    }

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    public OrderItem(Long productId, int quantity, String size, String name, Float price, Order order) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
        this.name = name;
        this.price = price;
        this.order = order;
    }
}