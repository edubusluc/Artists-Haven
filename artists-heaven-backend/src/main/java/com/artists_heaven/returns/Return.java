package com.artists_heaven.returns;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "returns")
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El motivo de la devolución es obligatorio")
    @Size(max = 500, message = "El motivo no debe superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String reason;

    @NotNull(message = "La fecha de devolución es obligatoria")
    @Column(nullable = false, updatable = false)
    private LocalDateTime returnDate = LocalDateTime.now();
}
