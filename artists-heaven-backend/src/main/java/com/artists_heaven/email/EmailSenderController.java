package com.artists_heaven.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/emails")
public class EmailSenderController {

    private final EmailSenderService emailService;

    public EmailSenderController(EmailSenderService emailService) {
        this.emailService = emailService;
    }    

    // Endpoint to send emails
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody Email email) {
        try {
            // Call the email service to send the report email
            emailService.sendReportEmail(email);

            // Return a success response if the email was sent successfully
            return ResponseEntity.ok("Email enviado exitosamente!");
        }catch (Exception e){
        //Return an error response with the exception message
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("No se ha podido enviar el email. Error: " + e.getMessage());
        }  
    }

}

