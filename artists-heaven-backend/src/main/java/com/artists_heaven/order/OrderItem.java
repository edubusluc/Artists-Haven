package com.artists_heaven.order;

import com.artists_heaven.product.Section;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "El ID del producto es obligatorio")
    @Column(nullable = false)
    private Long productId;

    private String size;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    @Column(nullable = false)
    private Float price;

    public OrderItem() {
    }

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    private Section section;

    public OrderItem(Long productId, int quantity, String size, String name, Float price, Order order, Section section) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
        this.name = name;
        this.price = price;
        this.order = order;
        this.section = section;
    }
}