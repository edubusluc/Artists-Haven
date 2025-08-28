package com.artists_heaven.rating;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer score;

    @Length(max = 255, message = "El comentario no debe superar los 255 caracteres")
    private String comment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    @JsonIgnore
    private User user;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    @JsonIgnore
    private Product product;

    @JsonProperty("email")
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }

}
