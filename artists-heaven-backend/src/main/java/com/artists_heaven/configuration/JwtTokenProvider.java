package com.artists_heaven.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.artists_heaven.entities.user.User;

import com.artists_heaven.entities.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Autowired
    private UserRepository userRepository;

    Dotenv dotenv = Dotenv.load();
    private String secretKey = dotenv.get("JWT_SECRET");


    // Método para obtener la clave secreta de forma segura
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Método para resolver el token desde el encabezado Authorization
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Método para validar el token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Usar la clave segura para validar
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // En caso de que el token sea inválido
        }
    }

    // Método para obtener la autenticación del usuario desde el token
    public Authentication getAuthentication(String token) {
        // Obtén las reclamaciones (claims) del token
        Claims claims = getClaims(token);
        String email = claims.getSubject(); // Obtén el correo electrónico desde el token

        // Busca el usuario en la base de datos usando el correo electrónico
        User myUser = userRepository.findByEmail(email);

        if (myUser == null) {
            // Lanza una excepción si el usuario no se encuentra
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Retorna el objeto Authentication con el usuario, token y sus autoridades
        return new UsernamePasswordAuthenticationToken(myUser, token, myUser.getAuthorities());
    }

    // Método para obtener los reclamos (claims) del token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Usar la clave segura para obtener los claims
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Método para generar un nuevo token JWT
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (900 * 1000));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Usar la clave segura para firmar
                .compact();
    }
}
