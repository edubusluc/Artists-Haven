package com.artists_heaven.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.lang.reflect.Method;

public class AuthServiceTest {

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
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mapRoleToAuthorityMethod = AuthService.class.getDeclaredMethod("mapRoleToAuthority", UserRole.class);
        mapRoleToAuthorityMethod.setAccessible(true);
    }

    @Test
    public void testLoginSuccess() {
        String email = "test@example.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setRole(UserRole.USER);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        String token = authService.login(email, password);

        assertEquals("jwtToken", token);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, "hashedPassword");
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    public void testLoginFailure() {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmail(email)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(email, password);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    public void testHandleGoogleLoginSuccess() throws Exception {
        String idTokenString = "idToken";
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");
        payload.set("given_name", "Test");
        payload.set("family_name", "User");
        payload.set("name", "Test User");

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        when(idToken.getPayload()).thenReturn(payload);

        when(tokenVerifier.verifyToken(idTokenString)).thenReturn(idToken);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        Map<String, String> response = authService.handleGoogleLogin(idTokenString);

        assertNotNull(response);
        assertEquals("jwtToken", response.get("token"));
        assertEquals("test@example.com", response.get("email"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testHandleGoogleLoginInvalidToken() throws Exception {
        String idTokenString = "invalidToken";

        when(tokenVerifier.verifyToken(idTokenString)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.handleGoogleLogin(idTokenString);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    public void testMapRoleToAuthority_User() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.USER);
        assertEquals("ROLE_USER", authority);
    }

    @Test
    public void testMapRoleToAuthority_Artist() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.ARTIST);
        assertEquals("ROLE_ARTIST", authority);
    }

    @Test
    public void testMapRoleToAuthority_Admin() throws Exception {
        String authority = (String) mapRoleToAuthorityMethod.invoke(authService, UserRole.ADMIN);
        assertEquals("ROLE_ADMIN", authority);
    }
}
