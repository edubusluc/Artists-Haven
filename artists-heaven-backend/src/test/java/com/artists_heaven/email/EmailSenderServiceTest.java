package com.artists_heaven.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.EmailSendException;
import com.artists_heaven.order.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

class EmailSenderServiceTest {

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
    void testSendReportEmail_ThrowsEmailSendException() {
        // Preparación
        Email email = new Email();
        email.setId(1L);
        email.setType(EmailType.BUG_REPORT);
        email.setUsername("testUser");
        email.setDescription("This is a test report");

        // Mockeamos el repositorio para devolver el mismo email
        when(emailSenderRepository.save(any(Email.class))).thenReturn(email);

        // Forzamos que mailSender lance excepción
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Ejecución y verificación de la excepción
        EmailSendException exception = assertThrows(
                EmailSendException.class,
                () -> emailSenderService.sendReportEmail(email));

        assertTrue(exception.getMessage().contains("Failed to send email to moderator"));

        // Verificaciones de interacciones
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

        when(pdfGeneratorService.generateInvoice(any(Long.class), any(Order.class), any(Float.class), any(Long.class)))
                .thenReturn(mockPdfContent);

        emailSenderService.sendPurchaseConfirmationEmail(userEmail, order, 0l);
        verify(mailSender, times(1)).send(mimeMessage);
        verify(pdfGeneratorService, times(1)).generateInvoice(eq(12345L), eq(order), eq(90.0f), eq(0l));
    }

    @Test
    void testgetEmailCount() {
        int year = 2024;
        List<Object[]> mockResults = List.of(
                new Object[] { EmailType.BUG_REPORT, 5L },
                new Object[] { EmailType.FEATURE_REQUEST, 2L });

        when(emailSenderRepository.countEmailsByType(year)).thenReturn(mockResults);

        Map<EmailType, Integer> result = emailSenderService.getEmailCounts(year);

        assertEquals(2, result.size());
        assertEquals(5, result.get(EmailType.BUG_REPORT));
        assertEquals(2, result.get(EmailType.FEATURE_REQUEST));

    }

    @Test
    public void testSendPasswordResetEmail() {
        // Arrange
        String to = "user@example.com";
        String link = "http://example.com/reset-password?token=xyz123";

        // Act
        emailSenderService.sendPasswordResetEmail(to, link);

        // Assert
        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setTo(to);
        expectedMessage.setSubject("Restablece tu contraseña");
        expectedMessage.setText("Haz clic en el siguiente enlace para restablecer tu contraseña: " + link);

        // Verificar que 'send' fue llamado una vez con el mensaje esperado
        verify(mailSender, times(1)).send(expectedMessage);
    }

    @Test
    void sendVerificationEmail_ThrowsEmailSendException() {
        // Preparación
        Artist artist = new Artist();
        artist.setArtistName("TestArtist");

        // Mockeamos mailSender para que lance excepción
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Ejecución y verificación
        AppExceptions.EmailSendException exception = assertThrows(
                AppExceptions.EmailSendException.class,
                () -> emailSenderService.sendVerificationEmail(artist));

        assertTrue(exception.getMessage().contains("Failed to send verification email for artist"));
        assertEquals("TestArtist", artist.getArtistName());
    }

}
