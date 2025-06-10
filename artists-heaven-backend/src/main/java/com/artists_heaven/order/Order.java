package com.artists_heaven.order;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;

    private LocalDate createdDate = LocalDate.now();

    private String email;

    private String phone;

    private String paymentIntent;

    public Order() {
    }

}
