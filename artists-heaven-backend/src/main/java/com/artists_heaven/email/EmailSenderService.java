package com.artists_heaven.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.artist.Artist;

@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;

    private final EmailSenderRepository emailSenderRepository;

    public EmailSenderService(JavaMailSender mailSender, EmailSenderRepository emailSenderRepository) {
        this.mailSender = mailSender;
        this.emailSenderRepository = emailSenderRepository;
    }

     // Constant email address used as the sender for reports and verification emails
    private static final String MODERATOR_EMAIL = "mod.artistheaven@gmail.com";

    /**
     * Sends a report email to the moderator and saves the email to the repository.
     *
     * @param email the email object containing report details
     */
    public void sendReportEmail(Email email) {
        // Save the email details to the database for record-keeping
        emailSenderRepository.save(email);

        // Create the subject line and body content for the report email
        String subject = email.getId() + " [" + email.getType() + "] User: " + email.getUsername();
        String body = "Username: " + email.getUsername()
                + "\n\nDescription:\n" + email.getDescription();

        // Send the email to the moderator
        sendEmail(MODERATOR_EMAIL, subject, body);
    }

    /**
     * Sends a verification email to the moderator for a new artist verification request.
     *
     * @param artist the artist object containing artist details
     */
    public void sendVerificationEmail(Artist artist) {
        // Create the subject line and body content for the report email
        String subject = "NEW [ARTIST_VERIFICATION] " + artist.getArtistName();
        String body = "Username: " + artist.getArtistName()
                + "\n\nDescription:\n" + "Ha solicitado un proceso para revisar el estado de su cuenta";

         // Send the email to the moderator
        sendEmail(MODERATOR_EMAIL, subject, body);
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
}
