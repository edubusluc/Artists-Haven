package com.artists_heaven.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private TokenVerifier tokenVerifier;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testLoginUser_withValidCredentials() throws Exception {

        String mockJwt = "mock-jwt-token";
        User user = new User();
        user.setEmail("email@example.com");
        user.setRole(UserRole.USER);

        when(authService.login(anyString(), anyString())).thenReturn(mockJwt);

        LoginRequest loginRequest = new LoginRequest("email@example.com", "password1234");
        when(userRepository.findByEmail("email@example.com")).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginUser_withUserNull() throws Exception {

        String mockJwt = "mock-jwt-token";
        User user = new User();
        user.setEmail("email@example.com");
        user.setRole(UserRole.USER);

        when(authService.login(anyString(), anyString())).thenReturn(mockJwt);

        LoginRequest loginRequest = new LoginRequest("wrongemail@email.com", "password1234");
        when(userRepository.findByEmail("email@example.com")).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginUser_withNullToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("wrongemail@email.com", "password1234");
        when(authService.login(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginUser_withNonValidCredentials() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));
        // Create a LoginRequest payload
        LoginRequest loginRequest = new LoginRequest("jane.doe@example.com", "wrongpassword");

        // Perform the POST request
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Expect 401 Unauthorized
    }

    @Test
    public void testGoogleLogin_withExistingUser() throws Exception {
        // Configurar el mock de TokenVerifier para devolver un token simulado
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(tokenVerifier.verifyToken(anyString())).thenReturn(mockToken);
        Payload mockPayload = mock(Payload.class);

        // Configurar el repositorio y el generador de tokens
        String email = "testuser@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(mockUser);
        when(mockToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("testuser@example.com");
        when(mockPayload.get("given_name")).thenReturn("Test");
        when(mockPayload.get("family_name")).thenReturn("User");
        when(mockPayload.get("name")).thenReturn("Test User");

        String token = "mockJwtToken";
        when(jwtTokenProvider.generateToken(any())).thenReturn(token);

        // Llamar al método y comprobar la respuesta
        Map<String, String> request = Map.of("idTokenString", "mockIdTokenString");
        ResponseEntity<Map<String, String>> response = authController.googleLogin(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody().get("token"));
        assertEquals(email, response.getBody().get("email"));

        // Verificar que los métodos mockeados se llamaron correctamente
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtTokenProvider, times(1)).generateToken(any());
    }

    @Test
    public void testGoogleLogin_withNonExistingUser() throws Exception {
        // Configurar el mock de TokenVerifier para devolver un token simulado
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        when(tokenVerifier.verifyToken(anyString())).thenReturn(mockToken);
        Payload mockPayload = mock(Payload.class);

        // Configurar el repositorio y el generador de tokens
        String email = "testuser@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        when(mockToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("testuser@example.com");
        when(mockPayload.get("given_name")).thenReturn("Test");
        when(mockPayload.get("family_name")).thenReturn("User");
        when(mockPayload.get("name")).thenReturn("Test User");

        String token = "mockJwtToken";
        when(jwtTokenProvider.generateToken(any())).thenReturn(token);

        // Llamar al método y comprobar la respuesta
        Map<String, String> request = Map.of("idTokenString", "mockIdTokenString");
        ResponseEntity<Map<String, String>> response = authController.googleLogin(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody().get("token"));
        assertEquals(email, response.getBody().get("email"));

        // Verificar que los métodos mockeados se llamaron correctamente
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtTokenProvider, times(1)).generateToken(any());
    }

    @Test
    public void testGoogleLogin_withUnauthorizedException() throws Exception {
        // Mock an exception when verifying the token
        when(jwtTokenProvider.generateToken(any(Authentication.class)))
                .thenThrow(new RuntimeException("Error verifying token"));

        // Create the request payload
        Map<String, String> requestPayload = Map.of("idTokenString", "mock-id-token");

        // Perform the POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/google-login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid ID token"));
    }

    @Test
    public void testGoogleLogin_withInternalServerErrorException() throws Exception {
        // Configurar el idTokenString de prueba
        String idTokenString = "invalidTokenString";

        // Configurar el comportamiento de tokenVerifier para lanzar una excepción
        when(tokenVerifier.verifyToken(idTokenString)).thenThrow(new RuntimeException("Token verification failed"));

        // Configurar la solicitud
        Map<String, String> request = Map.of("idTokenString", idTokenString);

        // Llamar al método y comprobar la respuesta
        ResponseEntity<Map<String, String>> response = authController.googleLogin(request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error verifying token", response.getBody().get("error"));

        // Verificar que el método de tokenVerifier se llamó con el argumento correcto
        verify(tokenVerifier, times(1)).verifyToken(idTokenString);
    }

}
