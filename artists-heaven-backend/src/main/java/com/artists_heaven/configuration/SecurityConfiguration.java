package com.artists_heaven.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
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
import org.springframework.security.config.Customizer;

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
                        "/api/artists/main",
                        "/api/artists/mainArtist_media/**",
                        "/api/auth/login",
                        "/api/auth/google-login",
                        "/api/emails/send",
                        "/accounts.google.com/**",
                        "/login/oauth2/code/google",
                        "/api/product/categories",
                        "/api/product/allProducts",
                        "/api/product/product_media/**",
                        "/api/product/sorted12Product",
                        "/api/product/details/{id}",
                        "/api/event/event_media/**",
                        "/api/myShoppingCart",
                        "/api/myShoppingCart/addProducts",
                        "/api/myShoppingCart/deleteProducts",
                        "/api/myShoppingCart/addProductsNonAuthenticate",
                        "/api/myShoppingCart/deleteProductsNonAuthenticated",
                        "/api/payment_process/checkout",
                        "/api/payment_process/stripeWebhook",
                        "/api/rating/productReview/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/returns/create",
                        "/api/returns/*/label",
                        "/api/orders/by-identifier",
                        "/api/artists/{id}",
                        "/api/product/tshirt",
                        "/api/product/pants",
                        "/api/product/accessories",
                        "/api/product/hoodies",
                        "/api/chatbot/**",
                        "/api/product/related",
                        "/api/product/by-reference",
                        "/api/product/promoted-collections",
                        "/api/product/collection/**",
                        "/api/event/futureEvents/**",
                        "/api/event/allFutureEvents",
                        "/api/payment_process/confirm",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password",
                        "/api/user-products/all",
                        "/api/user-products/userProduct_media/**",
                        "/api/product/allCollections",
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
                        "/api/admin/product-management",
                        "/api/admin/users",
                        "/api/admin/orders",
                        "/api/admin/updateStatus",
                        "/api/admin/orderDetails/{id}",
                        "/api/admin/newCategory",
                        "/api/admin/editCategory",
                        "/api/admin/newCollection",
                        "/api/admin/editCollection",
                        "/api/admin/userProduct/pending",
                        "/api/admin/userProduct/{id}/approve",
                        "/api/admin/userProduct/{id}/reject",
                        "/api/returns/{id}/return"
        };

        // Endpoints accessible only by ARTIST users
        private static final String[] ARTIST_ENDPOINTS = {
                        "/api/verification/send",
                        "/api/event/new",
                        "/api/event/allMyEvents",
                        "/api/event/edit/{id}",
                        "/api/artists/dashboard",
                        "/api/artists/sales/monthly",
                        "/api/event/isVerified",
                        "/api/event/details/{id}",
        };

        private static final String[] AUTHENTICATED_ENDPOINTS = {
                        "/api/users/profile/edit",
                        "/api/users/profile",
                        "/api/rating/new",
                        "/api/orders/myOrders",
                        "/api/orders/{id}",
                        "/api/user-products/create",
                        "/api/productVote/{id}",
                        "/api/reward-cards/**",
                        "/api/user-products/myUserProducts"

        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                configureHttpSecurity(http);
                return http.build();
        }

        private void configureHttpSecurity(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                                                .requestMatchers(ARTIST_ENDPOINTS).hasRole("ARTIST")
                                                .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("http://localhost:3000", true))
                                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                                // 👇 Añadir cabeceras de seguridad recomendadas
                                .headers(headers -> headers
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)) // 1 año
                                                .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options:
                                                                                               // nosniff
                                                .cacheControl(Customizer.withDefaults()) // Cache-Control: no-cache,
                                                                                         // no-store, must-revalidate
                                                .frameOptions(frame -> frame.deny())
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'; " +
                                                                                "script-src 'self'; " +
                                                                                "style-src 'self'; " +
                                                                                "img-src 'self' data: http://localhost:8080; "
                                                                                +
                                                                                "font-src 'self'; " +
                                                                                "connect-src 'self' http://localhost:8080; "
                                                                                +
                                                                                "object-src 'none'; " +
                                                                                "frame-ancestors 'none'; " +
                                                                                "base-uri 'self';")));

        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://192.168.1.52:3000"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                // Configuración general para toda la API
                source.registerCorsConfiguration("/**", configuration);

                // Configuración específica para Swagger UI
                CorsConfiguration swaggerConfig = new CorsConfiguration();
                swaggerConfig.setAllowedOrigins(List.of("http://localhost:3000"));
                swaggerConfig.setAllowedMethods(List.of("GET"));
                swaggerConfig.setAllowedHeaders(List.of("*"));
                swaggerConfig.setAllowCredentials(true);
                source.registerCorsConfiguration("/swagger-ui/**", swaggerConfig);
                source.registerCorsConfiguration("/v3/api-docs/**", swaggerConfig);

                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                // Bean for password encoding using BCrypt
                return new BCryptPasswordEncoder();
        }

        @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> removeHeaders() {
                return factory -> factory.addConnectorCustomizers(connector -> connector.setXpoweredBy(false));
        }
}
