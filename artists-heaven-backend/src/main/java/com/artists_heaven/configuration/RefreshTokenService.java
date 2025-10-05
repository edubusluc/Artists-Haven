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
     * Creates or updates a refresh token for the given user.
     * The token will be valid for 7 days.
     *
     * @param user the user to associate the refresh token with
     * @return the newly created or updated {@link RefreshToken}
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
     * Verifies if the given refresh token is still valid.
     * If the token has expired, it is removed from the repository
     * and a {@link RuntimeException} is thrown.
     *
     * @param token the {@link RefreshToken} to verify
     * @return the same token if it is still valid
     * @throws RuntimeException if the token has expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expirado. Por favor inicia sesión nuevamente.");
        }
        return token;
    }

    /**
     * Finds a refresh token by its string value.
     *
     * @param token the token string to look up
     * @return an {@link Optional} containing the {@link RefreshToken} if found
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Deletes all refresh tokens associated with a given user.
     * Typically used during logout.
     *
     * @param user the user whose tokens should be deleted
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
