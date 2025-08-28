package com.artists_heaven.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.standardResponse.StandardResponse;

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

    @PostMapping("/send")
    @Operation(summary = "Send an email", description = "Sends an email report using the provided email details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "500", description = "Failed to send email", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
    public ResponseEntity<StandardResponse<String>> sendEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email object containing the recipient, subject, body, and other details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Email.class))) @RequestBody Email email) {

        email.setCreatedAt(new Date());
        emailService.sendReportEmail(email);

        return ResponseEntity.ok(
                new StandardResponse<>("Email sent successfully!", "Email sent successfully!", HttpStatus.OK.value()));
    }

}
