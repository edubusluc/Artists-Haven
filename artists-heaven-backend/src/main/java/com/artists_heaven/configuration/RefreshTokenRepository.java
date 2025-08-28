package com.artists_heaven.configuration;


import com.artists_heaven.entities.user.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    RefreshToken findByUser(User user);
}
