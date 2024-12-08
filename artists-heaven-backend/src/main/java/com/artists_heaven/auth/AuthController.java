package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController{

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerifier tokenVerifier;

    public AuthController(AuthService authService, UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder, TokenVerifier tokenVerifier) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerifier = tokenVerifier;

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (token == null) {
                // Manejo de error si el token es nulo
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
    
            User user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                // Manejo de error si el usuario no se encuentra
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
    
            UserRole role = user.getRole();
    
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role);
            return new ResponseEntity<>(response, HttpStatus.OK);
    
        } catch (Exception e) {
            // Manejo de excepciones generales con logging opcional
            return new ResponseEntity<>(Map.of("error", "Invalid credentials or server error"), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<Map<String, String>> googleLogin(@RequestBody Map<String, String> request) {
        String idTokenString = request.get("idTokenString");

        try {
            GoogleIdToken idToken = tokenVerifier.verifyToken(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User user = userRepository.findByEmail(email);
                if (user == null) {
                    String randomPassword = UUID.randomUUID().toString();
                    String encodedPassword = passwordEncoder.encode(randomPassword);
                    user = new User();
                    user.setEmail(email);
                    user.setFirstName(payload.get("given_name").toString());
                    user.setLastName(payload.get("family_name").toString());
                    user.setPassword(encodedPassword);
                    user.setUsername(payload.get("name").toString());
                    user.setRole(UserRole.USER);
                    userRepository.save(user);
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, null, AuthorityUtils.createAuthorityList("USER"));

                String token = jwtTokenProvider.generateToken(authentication);

                Map<String, String> response = Map.of(
                        "token", token,
                        "email", email);

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("error", "Invalid ID token"), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error verifying token"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
