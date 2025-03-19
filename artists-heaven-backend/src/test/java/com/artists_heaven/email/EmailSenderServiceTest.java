package com.artists_heaven.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.order.Order;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailSenderRepository emailSenderRepository;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendReportEmail() {
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
    void testSendVerificationEmail() {
        Artist artist = new Artist();
        artist.setArtistName("testArtist");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailSenderService.sendVerificationEmail(artist);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testsendPurchaseConfirmationEmailTest() throws MessagingException {
        String userEmail = "userEmail@test.com";
        Order order = new Order();
        order.setId(1L);
        order.setIdentifier(12345L);
        order.setTotalPrice(90.0f);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] mockPdfContent = new byte[10];

        when(pdfGeneratorService.generateInvoice(any(Long.class), any(Order.class), any(Float.class)))
            .thenReturn(mockPdfContent);


        emailSenderService.sendPurchaseConfirmationEmail(userEmail, order);
    }

}
