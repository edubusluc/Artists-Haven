package com.artists_heaven.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.artists_heaven.entities.artist.Artist;

public class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailSenderRepository emailSenderRepository;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendReportEmail() {
        Email email = new Email();
        email.setId(1L);
        email.setType(EmailType.BUG_REPORT);
        email.setUsername("testUser");
        email.setDescription("This is a test report");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(emailSenderRepository.save(any(Email.class))).thenReturn(email);

        emailSenderService.sendReportEmail(email);

        verify(emailSenderRepository, times(1)).save(email);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendVerificationEmail() {
        Artist artist = new Artist();
        artist.setArtistName("testArtist");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailSenderService.sendVerificationEmail(artist);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
