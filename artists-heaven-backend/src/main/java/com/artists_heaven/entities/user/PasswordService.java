package com.artists_heaven.entities.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Changes the user's current password to a new one.
     *
     * @param user            the {@link User} whose password is to be changed
     * @param currentPassword the user's current password
     * @param newPassword     the new password to set
     * @throws IllegalArgumentException if the current password does not match
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Creates a password reset token for a user identified by email.
     * The token is valid for 30 minutes.
     *
     * @param email the email of the user requesting a password reset
     * @return the generated token, or null if no user exists with the given email
     */
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        return token;
    }

    /**
     * Resets the user's password using a valid reset token.
     *
     * @param token       the password reset token
     * @param newPassword the new password to set
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
