package com.artists_heaven.configuration;

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

    private final JwtAuthenticationFilter jwtAuthorizationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthorizationFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    // Public endpoints that do not require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/users/register",
            "/api/artists/register",
            "/api/auth/login",
            "/api/auth/google-login",
            "/api/emails/send",
            "/accounts.google.com/**",
            "/login/oauth2/code/google",
            "/api/users/list",
            "/api/product/categories",
            "/api/product/allProducts",
            "/api/product/product_media/**",
            "/api/product/details/{id}",
            "/api/event/event_media/**",
            "/api/myShoppingCart",
            "/api/myShoppingCart/addProducts",
            "/api/myShoppingCart/deleteProducts",
            "/api/event/allEvents",
            "/api/myShoppingCart/addProductsNonAuthenticate",
            "/api/myShoppingCart/deleteProductsNonAuthenticated",
            "/api/payment_process/checkout",
            "/api/payment_process/stripeWebhook",
            "/api/rating/productReview/**",
            "/api/product/allPromotedProducts",
    };

    // Endpoints accessible only by ADMIN users
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/admin/validate_artist",
            "/api/admin/verification/pending",
            "/api/admin/verification_media/**",
            "/api/product/delete/{id}",
            "/api/product/edit/{id}",
            "/api/product/new",
            "/api/product/promotion/{id}",
            "/api/product/demote/{id}",
            "/api/admin/staticsPerYear",
            "/api/admin/sales/monthly",
            "/api/admin/product-management"
    };

    // Endpoints accessible only by ARTIST users
    private static final String[] ARTIST_ENDPOINTS = {
            "/api/verification/send",
            "/api/event/new",
            "/api/event/allMyEvents",
            "/api/event/edit/{id}",
    };

    private static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/users/profile/edit",
            "/api/users/profile",
            "/api/rating/new",
            "/api/orders/myOrders",
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
                // // Disables Cross-Site Request Forgery (CSRF) protection as it is not needed
                // when using JWT for stateless authentication.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(ARTIST_ENDPOINTS).hasRole("ARTIST")
                        .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()
                        .anyRequest().authenticated())
                // Configures OAuth2 login with a default success URL
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("http://localhost:3000", true))
                // Adds the custom JWT authorization filter before the default username/password
                // filter
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Configures CORS settings
        CorsConfiguration configuration = new CorsConfiguration();
        // Allowed origins
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
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
