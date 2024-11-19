package com.artists_heaven;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArtistsHeavenBackendApplication {

    public static void main(String[] args) {
        // Load the .env file
        Dotenv dotenv = Dotenv.configure().directory("./.env").load();

        // Set system properties for Spring Boot to read
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_URL_TEST", dotenv.get("DB_URL_TEST"));
        System.out.println(dotenv.get("DB_URL_TEST"));

        SpringApplication.run(ArtistsHeavenBackendApplication.class, args);
    }
}
