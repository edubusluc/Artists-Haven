package com.artists_heaven.productVote;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.GlobalExceptionHandler;

class ProductVoteControllerTest {

    @Mock
    private ProductVoteService productVoteService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductVoteController productVoteController;

    private MockMvc mockMvc;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productVoteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user = new User();
        user.setId(1L);
    }

    @Test
    void votePositive_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        // Mock para messageSource
        when(messageSource.getMessage("vote.registeredSuccessfully", null, new Locale("en")))
                .thenReturn("Vote registered successfully");

        // Realizar la solicitud HTTP con MockMvc
        mockMvc.perform(post("/api/productVote/100")
                .param("lang", "en")
                .with(user(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vote registered successfully"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.status").value(201));

    }

}
