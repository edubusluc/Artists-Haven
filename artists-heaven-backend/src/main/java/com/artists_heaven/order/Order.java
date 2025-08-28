package com.artists_heaven.order;

import java.time.LocalDateTime;
import java.util.List;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.returns.Return;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "El identificador es obligatorio")
    @Column(unique = true, nullable = false)
    private Long identifier;

    @Positive(message = "El precio total debe ser positivo")
    @NotNull(message = "El precio total es obligatorio")
    @Column(nullable = false)
    private Float totalPrice;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado del pedido es obligatorio")
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "El código postal es obligatorio")
    @Column(nullable = false)
    private String postalCode;

    @NotBlank(message = "La ciudad es obligatoria")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "El país es obligatorio")
    @Column(nullable = false)
    private String country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false)
    private String phone;

    private String paymentIntent;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Return returnRequest;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime lastUpdateDateTime = LocalDateTime.now();

    private Long discountApplied;

    public Order() {
        // No-argument constructor required by JPA for entity instantiation via
        // reflection
    }


}
