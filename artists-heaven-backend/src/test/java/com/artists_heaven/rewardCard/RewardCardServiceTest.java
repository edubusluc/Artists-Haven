package com.artists_heaven.rewardCard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.rewardCard.RewardCardController.RedeemRequest;

class RewardCardServiceTest {

    @Mock
    private RewardCardRepository rewardCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RewardCardService rewardCardService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Usuario autenticado simulado
        user = new User();
        user.setId(1L);
        user.setPoints(1000);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void redeemRewardCard_success_with500Points() {
        RedeemRequest request = new RedeemRequest();
        request.setRequiredPoints(500);

        when(rewardCardRepository.existsByUserAndRedeemedFalse(user)).thenReturn(false);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("OK");
        when(rewardCardRepository.save(any(RewardCard.class))).thenAnswer(i -> i.getArgument(0));

        RewardCard response = rewardCardService.redeemRewardCard(request, "es");

        assertNotNull(response);
        verify(userRepository).save(user);
        verify(rewardCardRepository).save(any(RewardCard.class));
    }

    @Test
    void redeemRewardCard_fails_notEnoughPoints() {
        RedeemRequest request = new RedeemRequest();
        request.setRequiredPoints(2000);

        when(messageSource.getMessage(eq("rewardCard.notEnoughPoints"), any(), any(Locale.class)))
                .thenReturn("Not enough points");

        AppExceptions.BadRequestException ex = assertThrows(AppExceptions.BadRequestException.class,
                () -> rewardCardService.redeemRewardCard(request, "en"));

        assertEquals("Not enough points", ex.getMessage());
    }

    @Test
    void redeemRewardCard_fails_activeRewardCardExists() {
        RedeemRequest request = new RedeemRequest();
        request.setRequiredPoints(500);

        when(rewardCardRepository.existsByUserAndRedeemedFalse(user)).thenReturn(true);
        when(messageSource.getMessage(eq("rewardCard.activeRewardCardExists"), any(), any(Locale.class)))
                .thenReturn("Active reward card exists");

        AppExceptions.BadRequestException ex = assertThrows(AppExceptions.BadRequestException.class,
                () -> rewardCardService.redeemRewardCard(request, "en"));

        assertEquals("Active reward card exists", ex.getMessage());
    }

    @Test
    void redeemRewardCard_fails_invalidPointsTier() {
        RedeemRequest request = new RedeemRequest();
        request.setRequiredPoints(300);

        when(rewardCardRepository.existsByUserAndRedeemedFalse(user)).thenReturn(false);
        when(messageSource.getMessage(eq("rewardCard.invalidPointsTier"), any(), any(Locale.class)))
                .thenReturn("Invalid tier");

        AppExceptions.BadRequestException ex = assertThrows(AppExceptions.BadRequestException.class,
                () -> rewardCardService.redeemRewardCard(request, "en"));

        assertEquals("Invalid tier", ex.getMessage());
    }

    @Test
    void activateRewardCard_success_with950Points() {
        user.setPoints(1000);

        when(rewardCardRepository.save(any(RewardCard.class))).thenAnswer(i -> i.getArgument(0));

        RewardCard card = rewardCardService.activateRewardCard(user);

        assertNotNull(card);
        assertEquals(950, card.getRequiredPoints());
        assertEquals(15, card.getDiscountPercentage());
        assertEquals(user, card.getUser());
    }

    @Test
    void activateRewardCard_success_with500Points() {
        user.setPoints(600);

        when(rewardCardRepository.save(any(RewardCard.class))).thenAnswer(i -> i.getArgument(0));

        RewardCard card = rewardCardService.activateRewardCard(user);

        assertNotNull(card);
        assertEquals(500, card.getRequiredPoints());
        assertEquals(10, card.getDiscountPercentage());
        assertEquals(user, card.getUser());
    }

    @Test
    void activateRewardCard_fails_notEnoughPoints() {
        user.setPoints(100);

        assertThrows(IllegalArgumentException.class, () -> rewardCardService.activateRewardCard(user));
    }
}
