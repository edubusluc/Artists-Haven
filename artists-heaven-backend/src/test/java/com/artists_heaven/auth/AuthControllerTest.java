package com.artists_heaven.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import org.springframework.http.MediaType;
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
        // Crear un mapa simulado para la respuesta de handleGoogleLogin
        Map<String, String> mockResponse = Map.of("token", "mockJwtToken", "email", "user@example.com", "role", "USER");

        // Mockear el comportamiento del servicio
        when(authService.handleGoogleLogin(anyString())).thenReturn(mockResponse);

        // Crear la solicitud de login
        Map<String, String> requestPayload = Map.of("idTokenString", "mockedIdToken");

        // Realizar la solicitud POST y verificar el resultado
        mockMvc.perform(post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Verificar que el estado es OK
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("mockJwtToken")) // Verificar el token
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("user@example.com")); // Verificar el email
        }

        @Test
        public void testGoogleLoginInvalidToken() throws Exception {
            // Simular un mapa con un error
            Map<String, String> mockResponse = Map.of("error", "Invalid ID token");
    
            // Mockear el comportamiento del servicio para un ID token inválido
            when(authService.handleGoogleLogin(anyString())).thenReturn(mockResponse);
    
            // Crear la solicitud de login
            Map<String, String> requestPayload = Map.of("idTokenString", "invalidToken");
    
            // Realizar la solicitud POST y verificar el resultado
            mockMvc.perform(post("/api/auth/google-login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestPayload)))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized()) // Verificar que el estado es 401
                    .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid ID token")); // Verificar el mensaje de error
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
}
