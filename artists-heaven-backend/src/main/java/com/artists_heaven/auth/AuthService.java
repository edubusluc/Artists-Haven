package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.exception.AppExceptions;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.artists_heaven.configuration.JwtTokenProvider;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final TokenVerifier tokenVerifier;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TokenVerifier tokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenVerifier = tokenVerifier;
    }

    private final String invalidCredentialsMessage = "Credenciales inválidas";

    /**
     * Authenticates a user with the provided email and password, and generates a
     * JWT token.
     * 
     * @param email    the email address of the user attempting to log in
     * @param password the password of the user attempting to log in
     * @return a JWT token if authentication is successful
     * @throws IllegalArgumentException if the email or password is invalid
     */
    public String login(String email, String password) {

        // Retrieve the user from the database by email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            // Throw an exception if the user is not found
            throw new AppExceptions.ResourceNotFoundException("User not found with this email");
        }

        User user = optionalUser.get();

        // Check if the user exists and if the provided password matches the stored
        // hashed password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(invalidCredentialsMessage);
        }

        // Map the user's role to an appropriate authority string
        String role = mapRoleToAuthority(user.getRole());

        // Create an Authentication object with the user's email, password, and
        // authorities
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                password,
                AuthorityUtils.createAuthorityList(role));

        // Generate and return a JWT token using the Authentication object
        return jwtTokenProvider.generateToken(authentication);
    }

    /**
     * Maps a UserRole to a corresponding authority string for security purposes.
     * 
     * @param role the user's role in the system
     * @return the corresponding authority string
     * @throws IllegalArgumentException if the role is not recognized
     */
    private String mapRoleToAuthority(UserRole role) {
        switch (role) {
            case USER:
                return "ROLE_USER";
            case ARTIST:
                return "ROLE_ARTIST";
            case ADMIN:
                return "ROLE_ADMIN";
            default:
                throw new IllegalArgumentException("Rol desconocido: " + role);
        }
    }

    /**
     * Handles user login via Google authentication.
     *
     * @param idTokenString the Google ID token string provided by the client
     * @return a map containing the generated JWT token and the user's email
     * @throws IllegalArgumentException if the ID token is invalid
     * @throws Exception                for any errors during the token verification
     *                                  or user creation process
     */
    public Map<String, String> handleGoogleLogin(String idTokenString) throws Exception {
        // Verificar el token de Google
        GoogleIdToken idToken = tokenVerifier.verifyToken(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException(invalidCredentialsMessage);
        }

        // Extraer el payload y el email
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // Buscar usuario por email
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = createNewUser(payload, email);
            userRepository.save(user);
        }

        // Crear Authentication con el rol del usuario
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                AuthorityUtils.createAuthorityList(user.getRole().toString()));

        // Generar JWT
        String token = jwtTokenProvider.generateToken(authentication);

        // Respuesta con token, email y rol
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("role", user.getRole().toString());

        return response;
    }

    /**
     * Creates a new user using the payload of a verified Google ID token.
     *
     * @param payload the payload from the Google ID token
     * @param email   the user's email address
     * @return a new User instance with default values
     */
    private User createNewUser(GoogleIdToken.Payload payload, String email) {
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        User user = new User();
        user.setEmail(email);
        user.setFirstName(payload.get("given_name").toString());
        user.setLastName(payload.get("family_name").toString());
        user.setPassword(encodedPassword);
        user.setUsername(payload.get("name").toString());
        user.setRole(UserRole.USER);

        return user;
    }

    public String generateToken(User user) {
        return jwtTokenProvider.generateTokenUser(user);
    }

}
