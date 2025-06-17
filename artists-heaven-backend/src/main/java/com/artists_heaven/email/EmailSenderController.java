package com.artists_heaven.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Date;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/emails")
public class EmailSenderController {

    private final EmailSenderService emailService;

    public EmailSenderController(EmailSenderService emailService) {
        this.emailService = emailService;
    }

    @Operation(summary = "Send an email", description = "Sends an email report using the provided email details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Email sent successfully!"))),
            @ApiResponse(responseCode = "500", description = "Failed to send email", content = @Content(mediaType = "text/plain", schema = @Schema(example = "Failed to send email. Error: ...")))
    })
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email object containing the recipient, subject, body, and other details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Email.class))) @RequestBody Email email) {
        try {
            // Set the creation date and send the email
            email.setCreatedAt(new Date());
            emailService.sendReportEmail(email);

            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email. Error: " + e.getMessage());
        }
    }

}
