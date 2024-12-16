package com.artists_heaven.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.artists_heaven.configuration.JwtTokenProvider;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenVerifier tokenVerifier;

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
        User user = userRepository.findByEmail(email);

        // Check if the user exists and if the provided password matches the stored
        // hashed password
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
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
        // Verify the Google ID token using a token verifier
        GoogleIdToken idToken = tokenVerifier.verifyToken(idTokenString);

        // Check if the ID token is null (verification failed)
        if (idToken == null) {
            throw new IllegalArgumentException(invalidCredentialsMessage);
        }

        // Extract the payload from the verified ID token
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // Check if the user exists in the repository
        User user = userRepository.findByEmail(email);

        // If the user doesn't exist, create a new user and save it to the repository
        if (user == null) {
            user = createNewUser(payload, email);
            userRepository.save(user);
        }

        // Create an Authentication object for the user with a default "USER" role
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                AuthorityUtils.createAuthorityList("USER"));

        // Generate a JWT token for the authenticated user
        String token = jwtTokenProvider.generateToken(authentication);

        // Prepare the response map with the token and user's email
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);

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

}
