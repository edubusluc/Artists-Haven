package com.artists_heaven.rating;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RatingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
    }

    @Test
    void testGetProductRatings() throws Exception {
        Rating rating = new Rating();
        rating.setScore(5);
        rating.setComment("Great product");
        List<Rating> ratings = List.of(rating);

        when(ratingService.getProductRatings(1L)).thenReturn(ratings);

        mockMvc.perform(get("/api/rating/productReview/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great product"));
    }

    @Test
    void testCreateNewRating_Success() throws Exception {
        RatingRequestDTO ratingRequestDTO = new RatingRequestDTO();
        ratingRequestDTO.setProductId(1L);
        ratingRequestDTO.setScore(5);
        ratingRequestDTO.setComment("Great product");

        Rating rating = new Rating();
        rating.setScore(5);
        rating.setComment("Great product");

        User user = new User();
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ratingService.createRating(anyLong(), anyLong(), anyInt(), anyString())).thenReturn(rating);

        mockMvc.perform(post("/api/rating/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.comment").value("Great product"));
    }

    @Test
    void testCreateNewRating_Unauthorized() throws Exception {
        RatingRequestDTO ratingRequestDTO = new RatingRequestDTO();
        ratingRequestDTO.setProductId(1L);
        ratingRequestDTO.setScore(5);
        ratingRequestDTO.setComment("Great product");

        User user = new User();
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ratingService.createRating(anyLong(), anyLong(), anyInt(), anyString())).thenThrow(new RuntimeException("Unauthorized"));

        mockMvc.perform(post("/api/rating/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingRequestDTO)))
                .andExpect(status().isUnauthorized());
    }
}