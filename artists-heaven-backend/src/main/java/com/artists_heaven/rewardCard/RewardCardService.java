package com.artists_heaven.rewardCard;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.rewardCard.RewardCardController.RedeemRequest;

@Service
public class RewardCardService {
    private final RewardCardRepository rewardCardRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public RewardCardService(RewardCardRepository rewardCardRepository, UserRepository userRepository,
            MessageSource messageSource) {
        this.rewardCardRepository = rewardCardRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public RewardCard redeemRewardCard(RedeemRequest request, String lang) {
        User user = getAuthenticatedUser();
        Locale locale = new Locale(lang);

        int requiredPoints = request.getRequiredPoints();

        // Validar puntos suficientes
        if (user.getPoints() < requiredPoints) {
            String msg = messageSource.getMessage("rewardCard.notEnoughPoints", null, locale);
            throw new AppExceptions.BadRequestException(msg);
        }

        // Validar que no tenga reward card activa sin canjear
        boolean hasPendingRewardCard = rewardCardRepository.existsByUserAndRedeemedFalse(user);
        if (hasPendingRewardCard) {
            String msg = messageSource.getMessage("rewardCard.activeRewardCardExists", null, locale); 
            throw new AppExceptions.BadRequestException(msg);
        }

        // Determinar descuento
        int discountPercentage = getDiscountPercentage(requiredPoints, locale);
        if (discountPercentage == -1) {
            String msg = messageSource.getMessage("rewardCard.invalidPointsTier", null, locale);
            throw new AppExceptions.BadRequestException(msg);
        }

        // Restar puntos al usuario
        user.setPoints(user.getPoints() - requiredPoints);
        userRepository.save(user);

        // Crear y guardar RewardCard
        RewardCard card = new RewardCard();
        card.setUser(user);
        card.setRequiredPoints(requiredPoints);
        card.setDiscountPercentage(discountPercentage);
        card.setRedeemed(false);
        rewardCardRepository.save(card);

        
        return (card);
    }

    private int getDiscountPercentage(int requiredPoints, Locale locale) {
        return switch (requiredPoints) {
            case 500 -> 10;
            case 950 -> 15;
            default -> -1;
        };
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof User user)
            return user;
        throw new RuntimeException("User not authenticated");
    }

    public RewardCard activateRewardCard(User user) {
        if (user.getPoints() >= 950) {
            user.setPoints(user.getPoints() - 950);
            RewardCard card = new RewardCard(null, 950, 15, user, false, LocalDateTime.now(), null);
            return rewardCardRepository.save(card);
        } else if (user.getPoints() >= 500) {
            user.setPoints(user.getPoints() - 500);
            RewardCard card = new RewardCard(null, 500, 10, user, false, LocalDateTime.now(), null);
            return rewardCardRepository.save(card);
        } else {
            throw new IllegalArgumentException("El usuario no tiene puntos suficientes para canjear una card");
        }
    }

}
