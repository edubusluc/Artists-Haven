package com.artists_heaven.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/users/list").permitAll()  // Permitir acceso a la ruta sin autenticación
                    .requestMatchers("/api/users/register").permitAll()
                    .requestMatchers("/api/artists/register").permitAll()
                    .anyRequest().authenticated()  // Asegurarse de que cualquier otra solicitud requiere autenticación
            );
    
        return http.build();
    }
}
