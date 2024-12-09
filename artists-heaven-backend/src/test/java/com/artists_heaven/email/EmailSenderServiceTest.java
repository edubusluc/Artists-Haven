package com.artists_heaven.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.artists_heaven.entities.artist.Artist;

@SpringBootTest
public class EmailSenderServiceTest {

    @Autowired
    private EmailSenderService emailSenderService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private EmailSenderRepository emailSenderRepository;


    @Test
    public void testSendReportEmail() {
        // Preparar los datos de prueba
        Email email = new Email();
        email.setId(1L);
        email.setUsername("testUser");
        email.setType(EmailType.BUG_REPORT);
        email.setDescription("This is a test bug description.");

        // Llamar al método sendEmail
        emailSenderService.sendReportEmail(email);

        // Verificar que el repositorio guarda el email
        verify(emailSenderRepository, times(1)).save(email);

        // Verificar que se envía un correo con los detalles correctos
        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setTo("mod.artistheaven@gmail.com");
        expectedMessage.setSubject("1 [BUG_REPORT] User: testUser");
        expectedMessage.setText("Username: testUser\n\nDescription:\nThis is a test bug description.");
        expectedMessage.setFrom("mod.artistheaven@gmail.com");

        verify(mailSender, times(1)).send(expectedMessage);
    }

    @Test
    public void testSendVerificationEmail() {
        // Arrange
        Artist artist = new Artist();
        artist.setArtistName("Test Artist");

        // Act
        emailSenderService.sendVerificationEmail(artist);

        // Assert
        // Verifica que el método send de mailSender se llamó una vez
        verify(mailSender, times(1)).send((SimpleMailMessage) any()); // Usa any() si no quieres verificar el contenido
    }


}
