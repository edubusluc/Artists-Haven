package com.artists_heaven.entities.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.artists_heaven.shopping_cart.ShoppingCart;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 50)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no debe tener más de 50 caracteres")
    private String firstName;

    @Column(name = "last_name", length = 50)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no debe tener más de 50 caracteres")
    private String lastName;

    @Column(unique = true, name = "user_name", length = 30)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 30, message = "El nombre de usuario debe tener entre 4 y 30 caracteres")
    private String username;

    @Column(unique = true, name = "email", length = 100)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    private String email;

    @Column(name = "password")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;

    @Column(length = 15)
    @Size(min = 9, max = 15, message = "El número debe tener entre 10 y 15 dígitos")
    @Digits(integer = 15, fraction = 0, message = "El número debe contener solo dígitos")
    private String phone;

    @Column(length = 255)
    @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
    private String address;

    @Column(length = 10)
    @Size(max = 10, message = "El código postal no debe superar los 10 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9\\- ]*$", message = "El código postal tiene un formato inválido")
    private String postalCode;

    @Column(length = 50)
    @Size(max = 50, message = "La ciudad no debe superar los 50 caracteres")
    private String city;

    @Column(length = 50)
    @Size(max = 50, message = "El país no debe superar los 50 caracteres")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private ShoppingCart shoppingCart;

    @Min(0)
    Integer points = 0;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == UserRole.ARTIST) {
            return List.of(new SimpleGrantedAuthority("ROLE_ARTIST"));
        } else if (role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

}
