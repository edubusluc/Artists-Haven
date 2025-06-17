package com.artists_heaven.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EmailSenderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EmailSenderService emailService;

    @InjectMocks
    private EmailSenderController emailSenderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(emailSenderController).build();
    }

    @Test
    void testSendEmail() throws Exception {
        Email email = new Email();
        email.setId(1L);
        email.setUsername("testUser");
        email.setType(EmailType.BUG_REPORT);
        email.setDescription("This is a test bug description.");

        doNothing().when(emailService).sendReportEmail(any(Email.class));

        mockMvc.perform(post("/api/emails/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email enviado exitosamente!"));

        verify(emailService, times(1)).sendReportEmail(any(Email.class));
    }
}
