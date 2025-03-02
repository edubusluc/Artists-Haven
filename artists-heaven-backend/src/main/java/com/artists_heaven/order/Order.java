package com.artists_heaven.order;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long identifier;

    @Positive
    @NotNull
    private Float totalPrice;

    private OrderStatus status;

    private String addressLine1;

    private String addressLine2;

    private String postalCode;

    private String city;

    private String country;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    private Long userId;

    public Order() {
    }

}
