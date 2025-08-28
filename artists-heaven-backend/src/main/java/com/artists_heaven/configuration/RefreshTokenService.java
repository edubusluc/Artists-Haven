package com.artists_heaven.configuration;

import com.artists_heaven.entities.user.User;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Crea un nuevo refresh token válido por 7 días.
     */
    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user) {
        RefreshToken existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken != null) {
            existingToken.setToken(UUID.randomUUID().toString());
            existingToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
            return refreshTokenRepository.save(existingToken);
        } else {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
            refreshToken.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(refreshToken);
        }
    }

    /**
     * Verifica si el token ha expirado.
     * Si expiró, lo elimina y lanza excepción.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expirado. Por favor inicia sesión nuevamente.");
        }
        return token;
    }

    /**
     * Busca un refresh token por su string.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Elimina todos los tokens de un usuario.
     * Puedes usarlo en logout.
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
