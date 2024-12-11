package com.artists_heaven.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthorizationFilter;

    // Public endpoints that do not require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/users/register",
        "/api/artists/register",
        "/api/auth/login",
        "/api/auth/google-login",
        "/api/emails/send",
        "/accounts.google.com/**",
        "/login/oauth2/code/google"
    };

    // Endpoints accessible only by ADMIN users
    private static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/validate_artist",
        "/api/admin/verification/pending",
        "/api/admin/verification_media/**"
    };
    
    // Endpoints accessible only by ARTIST users
    private static final String[] ARTIST_ENDPOINTS = {
        "/api/verification/send"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureHttpSecurity(http);
        return http.build();
    }

    private void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
            // Configures Cross-Origin Resource Sharing (CORS)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configura CORS
            // Disables Cross-Site Request Forgery (CSRF) protection
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() 
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .requestMatchers(ARTIST_ENDPOINTS).hasRole("ARTIST")
                .anyRequest().authenticated()
            )
            // Configures OAuth2 login with a default success URL
            .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("http://localhost:3000", true))
             // Adds the custom JWT authorization filter before the default username/password filter
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
         // Configures CORS settings
        CorsConfiguration configuration = new CorsConfiguration();
        // Allowed origins
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Allowed HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); 
        // Allowed headers
        configuration.setAllowedHeaders(List.of("*")); 
         // Allows credentials such as cookies or authorization headers
        configuration.setAllowCredentials(true); 

        // Applies the CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Bean for password encoding using BCrypt
        return new BCryptPasswordEncoder();
    }
}
