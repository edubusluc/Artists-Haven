package com.artists_heaven.order;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailsDTO {

    private Long id;
    private Long identifier;
    private Float totalPrice;
    private OrderStatus status;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String city;
    private String country;
    private Long userId;
    private List<OrderItem> items;
    private LocalDate createdDate;
    private String email;
    private String phone;
    private String paymentIntent;

    public OrderDetailsDTO(Order order) {
        this.id = order.getId();
        this.identifier = order.getIdentifier();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.addressLine1 = order.getAddressLine1();
        this.addressLine2 = order.getAddressLine2();
        this.postalCode = order.getPostalCode();
        this.city = order.getCity();
        this.country = order.getCountry();
        this.userId = order.getUser() != null ? order.getUser().getId() : null;

        // Si Order tiene una lista de OrderItems, la asignas directamente o haces un
        // mapeo
        this.items = order.getItems();

        this.createdDate = order.getCreatedDate();
        this.email = order.getEmail();
        this.phone = order.getPhone();
        this.paymentIntent = order.getPaymentIntent();
    }
}
