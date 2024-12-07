package com.artists_heaven.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailSenderRepository emailSenderRepository;

    public void sendEmail(Email email) {
        emailSenderRepository.save(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("mod.artistheaven@gmail.com");
        message.setSubject(email.getId() + " [" + email.getType()+"]" + " User: " + email.getUsername()); // Asunto del correo
        message.setText("Username: " + email.getUsername()
        +"\n\nDescription:\n" + email.getDescription());
        message.setFrom("mod.artistheaven@gmail.com");

        mailSender.send(message);
    }
}
