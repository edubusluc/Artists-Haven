package com.artists_heaven.entities.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changePassword_CurrentPasswordIncorrect_ThrowsException() {
        // Arrange
        User user = new User();
        user.setPassword("encodedOldPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.changePassword(user, "wrongPassword", "newPassword"));

        assertEquals("Contraseña actual incorrecta", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_CurrentPasswordCorrect_UpdatesPassword() {
        // Arrange
        User user = new User();
        user.setPassword("encodedOldPassword");

        when(passwordEncoder.matches("correctPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        passwordService.changePassword(user, "correctPassword", "newPassword");

        // Assert
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void createPasswordResetToken_UserFound_CreatesAndReturnsToken() {
        // Arrange
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        String token = passwordService.createPasswordResetToken(email);

        // Assert
        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token));
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user) &&
                savedToken.getToken().equals(token) &&
                savedToken.getExpiryDate().isAfter(LocalDateTime.now())));
    }

    @Test
    void resetPassword_TokenNotFound_ThrowsException() {
        // Arrange
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.resetPassword(token, "newPass"));

        assertEquals("Token inválido", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_TokenExpired_ThrowsException() {
        // Arrange
        String token = "expired-token";
        PasswordResetToken resetToken = mock(PasswordResetToken.class);
        when(resetToken.isExpired()).thenReturn(true);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.resetPassword(token, "newPass"));

        assertEquals("Token expirado", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_TokenValid_UpdatesPasswordAndDeletesToken() {
        // Arrange
        String token = "valid-token";
        String newPassword = "newPass";

        User user = new User();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPass");

        // Act
        passwordService.resetPassword(token, newPassword);

        // Assert
        assertEquals("encodedPass", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(resetToken);
    }

}
