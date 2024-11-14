package com.artists_heaven.entities.artist;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ArtistTest {

    private Artist artist;

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Inicializa la entidad Artist antes de cada test
        artist = new Artist();
        artist.setFirstName("John");
        artist.setLastName("Doe");
        artist.setEmail("john.doe@example.com");
        artist.setPassword("password123");
        artist.setArtistName("John Doe Music");
        artist.setUrl("http://johndoe.com");

        ValidatorFactory factory = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // Configura el interpolador de mensajes
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testArtistName() {
        // Verifica que el nombre del artista esté correctamente asignado
        assertEquals("John Doe Music", artist.getArtistName());
    }

    @Test
    void testUrl() {
        // Verifica que la URL esté correctamente asignada
        assertEquals("http://johndoe.com", artist.getUrl());
    }

    @Test
    void testArtistInheritance() {
        // Verifica que la clase Artist herede correctamente de User
        assertEquals("John", artist.getFirstName());
        assertEquals("Doe", artist.getLastName());
        assertEquals("john.doe@example.com", artist.getEmail());
    }

    @Test
    void testInvalidUrl() {
        // Crea un objeto Artist con una URL inválida
        Artist artist = new Artist();
        artist.setUrl("invalid-url");

        // Realiza la validación
        Set<ConstraintViolation<Artist>> violations = validator.validate(artist);

        // Verifica que haya una violación de la URL
        assertFalse(violations.isEmpty(), "Se esperaba una violación de validación");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("La URL proporcionada no es válida")),
                "Se esperaba el mensaje de error adecuado");
    }
}
