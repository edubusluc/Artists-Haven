package com.artists_heaven.productVote;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.userProduct.UserProduct;
import com.artists_heaven.userProduct.UserProductRepository;

class ProductVoteServiceTest {

    @Mock
    private ProductVoteRepository productVoteRepository;

    @Mock
    private UserProductRepository userProductRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductVoteService productVoteService;

    private User user;
    private User owner;
    private UserProduct product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        owner = new User();
        owner.setId(2L);
        owner.setPoints(10);

        product = new UserProduct();
        product.setId(100L);
        product.setOwner(owner);
        product.setNumVotes(0);
    }

    @Test
    void votePositive_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageSource.getMessage("user.NotFound", null, Locale.ENGLISH)).thenReturn("User not found");

        AppExceptions.ResourceNotFoundException ex = assertThrows(
            AppExceptions.ResourceNotFoundException.class,
            () -> productVoteService.votePositive(100L, 1L, "en")
        );
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void votePositive_productNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.findById(100L)).thenReturn(Optional.empty());
        when(messageSource.getMessage("product.not_found", null, Locale.ENGLISH)).thenReturn("Product not found");

        AppExceptions.ResourceNotFoundException ex = assertThrows(
            AppExceptions.ResourceNotFoundException.class,
            () -> productVoteService.votePositive(100L, 1L, "en")
        );
        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void votePositive_voteOwnProduct_throwsException() {
        product.setOwner(user); // mismo usuario
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.findById(100L)).thenReturn(Optional.of(product));
        when(messageSource.getMessage("vote.ownProduct", null, Locale.ENGLISH)).thenReturn("Cannot vote own product");

        AppExceptions.BadRequestException ex = assertThrows(
            AppExceptions.BadRequestException.class,
            () -> productVoteService.votePositive(100L, 1L, "en")
        );
        assertEquals("Cannot vote own product", ex.getMessage());
    }

    @Test
    void votePositive_alreadyVoted_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productVoteRepository.existsByUserAndProduct(user, product)).thenReturn(true);
        when(messageSource.getMessage("vote.alreadyVote", null, Locale.ENGLISH)).thenReturn("Already voted");

        AppExceptions.BadRequestException ex = assertThrows(
            AppExceptions.BadRequestException.class,
            () -> productVoteService.votePositive(100L, 1L, "en")
        );
        assertEquals("Already voted", ex.getMessage());
    }

    @Test
    void votePositive_success_voteSavedAndPointsUpdated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProductRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productVoteRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        productVoteService.votePositive(100L, 1L, "en");

        verify(productVoteRepository, times(1)).save(any());
        assertEquals(1, product.getNumVotes());
        assertEquals(15, owner.getPoints()); // 10 + 5 puntos por voto
        verify(userProductRepository, times(1)).save(product);
        verify(userRepository, times(1)).save(owner);
    }
}
