package com.artists_heaven.auth;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Jwts.builder()
                        .setSubject(user.getEmail())
                        .claim("role", user.getRole().name())
                        .signWith(jwtKey, SignatureAlgorithm.HS256)
                        .compact();
            }
        }
        throw new IllegalArgumentException("Credenciales inválidas");
    }
    
}
