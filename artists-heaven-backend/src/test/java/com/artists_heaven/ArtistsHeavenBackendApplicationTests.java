package com.artists_heaven;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")  // Activa el perfil 'test' para las pruebas
@TestPropertySource("classpath:application-test.properties")
class ArtistsHeavenBackendApplicationTests {

    @BeforeAll
    static void setup() {
        // Load the .env file
        Dotenv dotenv = Dotenv.configure().directory("../.env").load();

        System.setProperty("DB_URL_TEST", dotenv.get("DB_URL_TEST"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
    }
}