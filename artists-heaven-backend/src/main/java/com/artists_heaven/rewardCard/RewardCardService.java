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

    /**
     * Redeems a reward card for the authenticated user.
     *
     * @param request the redeem request containing required points
     * @param lang    user's language for localized messages
     * @return the newly created RewardCard
     * @throws AppExceptions.BadRequestException if the user doesn't have enough
     *                                           points,
     *                                           or already has a pending reward
     *                                           card
     */
    public RewardCard redeemRewardCard(RedeemRequest request, String lang) {
        User user = getAuthenticatedUser();
        Locale locale = new Locale(lang);

        int requiredPoints = request.getRequiredPoints();

        if (user.getPoints() < requiredPoints) {
            String msg = messageSource.getMessage("rewardCard.notEnoughPoints", null, locale);
            throw new AppExceptions.BadRequestException(msg);
        }

        boolean hasPendingRewardCard = rewardCardRepository.existsByUserAndRedeemedFalse(user);
        if (hasPendingRewardCard) {
            String msg = messageSource.getMessage("rewardCard.activeRewardCardExists", null, locale);
            throw new AppExceptions.BadRequestException(msg);
        }

        int discountPercentage = getDiscountPercentage(requiredPoints, locale);
        if (discountPercentage == -1) {
            String msg = messageSource.getMessage("rewardCard.invalidPointsTier", null, locale);
            throw new AppExceptions.BadRequestException(msg);
        }

        user.setPoints(user.getPoints() - requiredPoints);
        userRepository.save(user);

        RewardCard card = new RewardCard();
        card.setUser(user);
        card.setRequiredPoints(requiredPoints);
        card.setDiscountPercentage(discountPercentage);
        card.setRedeemed(false);
        rewardCardRepository.save(card);

        return (card);
    }

    /**
     * Determines discount based on required points.
     *
     * @param requiredPoints points required to redeem the card
     * @return discount percentage or -1 if invalid
     */
    private int getDiscountPercentage(int requiredPoints, Locale locale) {
        return switch (requiredPoints) {
            case 500 -> 10;
            case 950 -> 15;
            default -> -1;
        };
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return authenticated User
     * @throws RuntimeException if user is not authenticated
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof User user)
            return user;
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Activates a reward card for a user based on their points.
     *
     * @param user the user to activate the card for
     * @return the created RewardCard
     * @throws IllegalArgumentException if the user does not have enough points
     */
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
