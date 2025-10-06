package com.artists_heaven.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.configuration.JwtTokenProvider;
import com.artists_heaven.configuration.RefreshToken;
import com.artists_heaven.configuration.RefreshTokenRepository;
import com.artists_heaven.configuration.RefreshTokenService;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.user.PasswordService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

class AuthControllerTest {

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

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        System.out.println("passwordService = " + passwordService);
        System.out.println("emailSenderService = " + emailSenderService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testLoginUser_withValidCredentials() throws Exception {

        String mockJwt = "mock-jwt-token";
        User user = new User();
        user.setEmail("email@example.com");
        user.setRole(UserRole.USER);

        RefreshToken refreshToken = new RefreshToken();

        when(authService.login(anyString(), anyString())).thenReturn(mockJwt);

        LoginRequest loginRequest = new LoginRequest("email@example.com", "password1234");
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));

        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginUser_withUserNull() throws Exception {

        String mockJwt = "mock-jwt-token";
        User user = new User();
        user.setEmail("email@example.com");
        user.setRole(UserRole.USER);

        when(authService.login(anyString(), anyString())).thenReturn(mockJwt);

        LoginRequest loginRequest = new LoginRequest("wrongemail@email.com", "password1234");
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginUser_withNullToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("wrongemail@email.com", "password1234");
        when(authService.login(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginUser_withNonValidCredentials() throws Exception {
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
    void testGoogleLogin_withExistingUser() throws Exception {
        // Crear un mapa simulado para la respuesta de handleGoogleLogin
        Map<String, String> mockResponse = Map.of("token", "mockJwtToken", "email", "user@example.com", "role", "USER");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mockJwtToken");
        // Mockear el comportamiento del servicio
        User user = new User();
        user.setId(1l);

        when(authService.handleGoogleLogin(anyString())).thenReturn(mockResponse);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(refreshToken);

        // Crear la solicitud de login
        Map<String, String> requestPayload = Map.of("idTokenString", "mockedIdToken");

        // Realizar la solicitud POST y verificar el resultado
        mockMvc.perform(post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Verificar que el estado es OK
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("mockJwtToken")) // Verificar el token
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("user@example.com")); // Verificar el email
    }

    @Test
    void testGoogleLoginInvalidToken() throws Exception {
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid ID token")); // Verificar el mensaje
                                                                                                 // de error
    }

    @Test
    void testGoogleLogin_withUnauthorizedException() throws Exception {
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
    void testRefreshToken_withValidToken() throws Exception {
        User user = new User();
        user.setEmail("email@example.com");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-refresh-token");
        oldToken.setUser(user);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-refresh-token");
        newToken.setUser(user);

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.verifyExpiration(oldToken)).thenReturn(oldToken);
        when(refreshTokenService.createOrUpdateRefreshToken(user)).thenReturn(newToken);
        when(authService.generateToken(user)).thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", "old-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void testRefreshToken_withMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Refresh token is required"));
    }

    @Test
    void testRefreshToken_withInvalidToken() throws Exception {
        when(refreshTokenService.findByToken("invalid-refresh-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", "invalid-refresh-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid or expired refresh token"));
    }

    @Test
    void testRefreshToken_withExpiredToken() throws Exception {
        User user = new User();
        user.setEmail("email@example.com");

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setUser(user);

        when(refreshTokenService.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        when(refreshTokenService.verifyExpiration(expiredToken))
                .thenThrow(new RuntimeException("Expired"));

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", "expired-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid or expired refresh token"));
    }

    @Test
    void testChangePassword_success() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass456");

        // No necesitamos mockear passwordService.changePassword, porque no devuelve
        // nada,
        // solo asegurarnos de que no lanza excepción.
        doNothing().when(passwordService).changePassword(mockUser, "oldPass123", "newPass456");

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "user") // Simula un principal
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Contraseña actualizada"));
    }

    @Test
    void testChangePassword_withInvalidCurrentPassword() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");

        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPass", "newPass456");

        // Simulamos error en el servicio (ej. contraseña actual incorrecta)
        doThrow(new IllegalArgumentException("Contraseña actual incorrecta"))
                .when(passwordService).changePassword(any(User.class), eq("wrongOldPass"), eq("newPass456"));

        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "user")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testForgotPassword_success() throws Exception {
        String email = "user@example.com";
        String mockToken = "reset-token-123";

        // Simulas la respuesta del servicio
        when(passwordService.createPasswordResetToken(email)).thenReturn(mockToken);

        // Simulas el envío del correo
        doNothing().when(emailSenderService)
                .sendPasswordResetEmail(eq(email), eq("http://localhost:3000/reset-password?token=" + mockToken));

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Correo enviado si la cuenta existe"));
    }

    @Test
    void testForgotPassword_serviceThrowsException() throws Exception {
        String email = "user@example.com";

        when(passwordService.createPasswordResetToken(email))
                .thenThrow(new RuntimeException("Error creating token"));

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testResetPassword_success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token-123", "newPassword456");

        // Simulamos que el servicio no lanza excepción
        doNothing().when(passwordService).resetPassword("valid-token-123", "newPassword456");

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Contraseña restablecida"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
