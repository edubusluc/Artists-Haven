package com.artists_heaven.rewardCard;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RewardCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RewardCardRepository rewardCardRepository;

    @Mock
    private RewardCardService rewardCardService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private RewardCardController rewardCardController;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(rewardCardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        user = new User();
        user.setId(1L);
        user.setPoints(800);
        objectMapper = new ObjectMapper();

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMyPoints_success() throws Exception {
        mockMvc.perform(get("/api/reward-cards/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User points fetched successfully"))
                .andExpect(jsonPath("$.data").value(800))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getMyRewardCards_success() throws Exception {
        RewardCard card = new RewardCard(1L, 500, 10, user, false, null, null);

        when(rewardCardRepository.findByUser(user)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/reward-cards/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reward cards fetched successfully"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].requiredPoints").value(500))
                .andExpect(jsonPath("$.data[0].discountPercentage").value(10))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void redeemRewardCard_success() throws Exception {
        RewardCardController.RedeemRequest request = new RewardCardController.RedeemRequest();
        request.setRequiredPoints(500);

        RewardCard card = new RewardCard(1L, 500, 10, user, false, null, null);
        when(rewardCardService.redeemRewardCard(any(), any())).thenReturn(card);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Reward card redeemed");

        mockMvc.perform(post("/api/reward-cards/redeem")
                .param("lang", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reward card redeemed"))
                .andExpect(jsonPath("$.data.requiredPoints").value(500))
                .andExpect(jsonPath("$.data.discountPercentage").value(10))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getMyPoints_fails_whenUserNotAuthenticated() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("notAUser", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/reward-cards/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not authenticated"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
