package com.artists_heaven.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.artists_heaven.rating.Rating;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "La descripción no debe superar los 1000 caracteres")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que 0")
    @Column(nullable = false)
    private Float price;

    @NotNull
    @Column(nullable = false)
    private Boolean available = false;

    @ManyToMany
    @JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    private Boolean on_Promotion = false;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 100, message = "El descuento no puede ser mayor al 100%")
    private Integer discount = 0;

    @CreationTimestamp
    private Date createdDate;

    @NotNull(message = "La sección del producto es obligatoria")
    @Enumerated(EnumType.STRING)
    private Section section;

    @Column(columnDefinition = "TEXT")
    @NotBlank
    private String composition;

    @Column(columnDefinition = "TEXT")
    @NotBlank
    private String shippingDetails;

    @NotNull(message = "La referencia es obligatoria")
    @Positive(message = "La referencia debe ser un número positivo")
    @Column(unique = true, nullable = false)
    private Long reference;

    @ManyToOne
    Collection collection;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductColor> colors = new ArrayList<>();
}
