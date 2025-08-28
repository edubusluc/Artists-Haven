package com.artists_heaven.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.lang.reflect.Method;

class AuthServiceTest {

    private Method mapRoleToAuthorityMethod;

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenVerifier tokenVerifier;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mapRoleToAuthorityMethod = AuthService.class.getDeclaredMethod("mapRoleToAuthority", UserRole.class);
        mapRoleToAuthorityMethod.setAccessible(true);
    }

    @Test
    void testLoginSuccess() {
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setRole(UserRole.USER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        String token = authService.login(email, password);

        assertEquals("jwtToken", token);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    void testLoginFailure() {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(email, password);
        });

        assertEquals("User not found with this email", exception.getMessage());
    }

    @Test
    void testHandleGoogleLoginSuccess() throws Exception {
        String idTokenString = "idToken";
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");
        payload.set("given_name", "Test");
        payload.set("family_name", "User");
        payload.set("name", "Test User");

        User user = new User();
        user.setId(1l);
        user.setRole(UserRole.USER);

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(idToken.getPayload()).thenReturn(payload);

        when(tokenVerifier.verifyToken(idTokenString)).thenReturn(idToken);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        Map<String, String> response = authService.handleGoogleLogin(idTokenString);

        assertNotNull(response);
        assertEquals("jwtToken", response.get("token"));
        assertEquals("test@example.com", response.get("email"));

    }

    @Test
    void testHandleGoogleLoginSuccessWithNullUser() throws Exception {
        String idTokenString = "idToken";
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");
        payload.set("given_name", "Test");
        payload.set("family_name", "User");
        payload.set("name", "Test User");

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(idToken.getPayload()).thenReturn(payload);

        when(tokenVerifier.verifyToken(idTokenString)).thenReturn(idToken);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        Map<String, String> response = authService.handleGoogleLogin(idTokenString);

        assertNotNull(response);
        assertEquals("jwtToken", response.get("token"));
        assertEquals("test@example.com", response.get("email"));

    }

    @Test
    void testHandleGoogleLoginInvalidToken() throws Exception {
        String idTokenString = "invalidToken";

        when(tokenVerifier.verifyToken(idTokenString)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.handleGoogleLogin(idTokenString);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    void testMapRoleToAuthority_User() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.USER);
        assertEquals("ROLE_USER", authority);
    }

    @Test
    void testMapRoleToAuthority_Artist() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.ARTIST);
        assertEquals("ROLE_ARTIST", authority);
    }

    @Test
    void testMapRoleToAuthority_Admin() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.ADMIN);
        assertEquals("ROLE_ADMIN", authority);
    }

    @Test
    void generateToken_WithUser_CallsJwtTokenProvider() {
        User user = new User();
        String expectedToken = "mocked-jwt-token";

        when(jwtTokenProvider.generateTokenUser(user)).thenReturn(expectedToken);

        String token = authService.generateToken(user);

        assertEquals(expectedToken, token);
        verify(jwtTokenProvider).generateTokenUser(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
