package com.artists_heaven;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Activa el perfil 'test' para las pruebas
class ArtistsHeavenBackendApplicationTests {

    @BeforeAll
    static void setup() {
        // Load the .env file
        Dotenv dotenv = Dotenv.configure().directory("../.env").load();

        System.setProperty("DB_URL_TEST", dotenv.get("DB_URL_TEST"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

    }

    @Test
    void contextLoads() {
        // Aquí van tus pruebas
    }
}