package com.artists_heaven.productVote;

import java.util.Date;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.userProduct.UserProduct;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Usuario que vota

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private UserProduct product; // Producto votado

    private Date votedAt = new Date();
}
