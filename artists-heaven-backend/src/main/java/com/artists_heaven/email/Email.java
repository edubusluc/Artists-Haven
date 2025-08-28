package com.artists_heaven.email;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El asunto no puede estar vacío")
    @Size(max = 150, message = "El asunto no debe superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String subject;

    @NotBlank(message = "El remitente es obligatorio")
    @jakarta.validation.constraints.Email(message = "Debe ser un correo electrónico válido")
    @Column(nullable = false)
    private String sender;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no debe superar los 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 100, message = "El nombre de usuario no debe superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El tipo de correo es obligatorio")
    @Column(nullable = false, length = 50)
    private EmailType type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();

}
