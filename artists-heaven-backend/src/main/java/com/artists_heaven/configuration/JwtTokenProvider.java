package com.artists_heaven.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.artists_heaven.entities.user.User;

import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Load the secret key from environment variables
    Dotenv dotenv = Dotenv.load();
    private String secretKey = dotenv.get("JWT_SECRET");

    // Method to retrieve the signing key securely
    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Method to extract the token from the Authorization header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix to get the token
            return bearerToken.substring(7);
        }
        return null;
    }

    // Method to validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    // Use the signing key to validate the token
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Method to retrieve authentication information from the token
    public Authentication getAuthentication(String token) {
        // Extract claims from the token
        Claims claims = getClaims(token);
        // Retrieve the email (subject) from the token
        String email = claims.getSubject();

        // Look up the user in the database by email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            // Throw an exception if the user is not found
            throw new AppExceptions.ResourceNotFoundException("User not found with this email");
        }

        User myUser = optionalUser.get();

        // Return an Authentication object with the user, token, and authorities
        return new UsernamePasswordAuthenticationToken(myUser, token, myUser.getAuthorities());
    }

    // Method to extract claims (payload) from the token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Method to generate a new JWT token
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (3600 * 1000));
        String email = "";

        // Retrieve the email from the authenticated user
        /**
         * authentication.getPrincipal() instanceof User -> login by web form
         * email = authentication.getName(); -> login by google
         */
        if (authentication.getPrincipal() instanceof User) {
            User userDetails = (User) authentication.getPrincipal();
            email = userDetails.getEmail();
        } else {
            email = authentication.getName();
        }

        // Build and return the JWT token
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Sign the token with the secure key
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenUser(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (3600 * 1000)); // 1 hora

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
