package com.artists_heaven.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configura CORS para permitir solicitudes de localhost:3000
        registry.addMapping("/**")  // Aplica CORS a todas las rutas
                .allowedOrigins("http://localhost:3000")  // Origen permitido (tu frontend)
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Métodos permitidos
                .allowedHeaders("*")  // Todos los encabezados permitidos
                .allowCredentials(true);  // Si necesitas enviar cookies o credenciales
    }
}
