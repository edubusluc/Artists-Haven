package com.artists_heaven.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.order.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.EmailSendException;

@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;

    private final EmailSenderRepository emailSenderRepository;

    private final PdfGeneratorService pdfGeneratorService;

    public EmailSenderService(JavaMailSender mailSender, EmailSenderRepository emailSenderRepository,
            PdfGeneratorService pdfGeneratorService) {
        this.mailSender = mailSender;
        this.emailSenderRepository = emailSenderRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    // Constant email address used as the sender for reports and verification emails
    private static final String MODERATOR_EMAIL = "mod.artistheaven@gmail.com";

    /**
     * Sends a report email to the moderator and saves the email to the repository.
     *
     * @param email the email object containing report details
     */
    public void sendReportEmail(Email email) {
        // Save email record
        emailSenderRepository.save(email);

        // Build email content
        String subject = email.getId() + " [" + email.getType() + "] User: " + email.getUsername();
        String body = "Username: " + email.getUsername()
                + "\n\nDescription:\n" + email.getDescription();

        // Send the email
        try {
            sendEmail(MODERATOR_EMAIL, subject, body);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send email to moderator", e);
        }
    }

    /**
     * Sends a verification email to the moderator for a new artist verification
     * request.
     *
     * @param artist the artist object containing artist details
     */
    public void sendVerificationEmail(Artist artist) {
        try {
            String subject = "NEW [ARTIST_VERIFICATION] " + artist.getArtistName();
            String body = "Username: " + artist.getArtistName()
                    + "\n\nDescription:\n" + "Ha solicitado un proceso para revisar el estado de su cuenta";

            sendEmail(MODERATOR_EMAIL, subject, body);
        } catch (Exception e) {
            throw new AppExceptions.EmailSendException(
                    "Failed to send verification email for artist: " + artist.getArtistName(), e);
        }
    }

    public void sendPurchaseConfirmationEmail(String userEmail, Order order, Long discount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Long orderReference = order.getIdentifier();
        Float total = order.getTotalPrice();

        helper.setTo(userEmail);
        helper.setSubject("Confirmación de compra - " + orderReference);
        helper.setText("Gracias por tu compra. Adjuntamos tu factura.");

        // Generar PDF
        byte[] pdfBytes = pdfGeneratorService.generateInvoice(orderReference, order, total, discount);
        InputStreamSource pdfSource = new ByteArrayResource(pdfBytes);

        // Adjuntar PDF al correo
        helper.addAttachment("Factura_" + orderReference + ".pdf", pdfSource);

        mailSender.send(message);
    }

    /**
     * Helper method to send an email with a specified recipient, subject, and body.
     *
     * @param to      the recipient email address
     * @param subject the subject of the email
     * @param body    the body content of the email
     */
    private void sendEmail(String to, String subject, String body) {
        // Create a simple mail message object
        SimpleMailMessage message = new SimpleMailMessage();
        // Set the recipient of the email
        message.setTo(to);
        // Set the subject of the email
        message.setSubject(subject);
        // Set the body content of the email
        message.setText(body);
        // Set the sender email address
        message.setFrom(MODERATOR_EMAIL);
        // Send the constructed email message
        mailSender.send(message);
    }

    public Map<EmailType, Integer> getEmailCounts(int year) {

        List<Object[]> result = emailSenderRepository.countEmailsByType(year);
        Map<EmailType, Integer> emailCountMap = new HashMap<>();

        for (Object[] row : result) {
            EmailType type = (EmailType) row[0];
            Long count = (Long) row[1];
            emailCountMap.put(type, count.intValue());
        }

        return emailCountMap;
    }

    public void sendPasswordResetEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Restablece tu contraseña");
        message.setText("Haz clic en el siguiente enlace para restablecer tu contraseña: " + link);
        mailSender.send(message);
    }
}
