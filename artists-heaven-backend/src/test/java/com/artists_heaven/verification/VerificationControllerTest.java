package com.artists_heaven.verification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.artists_heaven.images.ImageServingUtil;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class VerificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private VerificationService verificationService;

    @Mock
    private ImageServingUtil imageServingUtil;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private VerificationController verificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(verificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testSendValidation_Success() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "dummy".getBytes());
        String email = "artist@example.com";
        String lang = "en";

        Artist artist = new Artist();
        artist.setEmail(email);

        when(verificationService.validateArtistForVerification(email)).thenReturn(artist);
        when(imageServingUtil.saveImages(any(), anyString(), anyString(), anyBoolean()))
                .thenReturn("http://example.com/video.mp4");
        when(messageSource.getMessage(eq("verification.message.successful"), isNull(), any(Locale.class)))
                .thenReturn("Verification successful");

        mockMvc.perform(multipart("/api/verification/send")
                .file(video)
                .param("email", email)
                .param("lang", lang)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification successful"))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
    }

    @Test
    void testSendValidation_ArtistNotEligible() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "dummy".getBytes());
        String email = "artist@example.com";

        when(verificationService.validateArtistForVerification(email))
                .thenThrow(new AppExceptions.ForbiddenActionException("You are not allowed to verify"));

        mockMvc.perform(multipart("/api/verification/send")
                .file(video)
                .param("email", email)
                .param("lang", "en")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to verify"))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testSendValidation_DuplicateRequest() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "dummy".getBytes());
        String email = "artist@example.com";

        Artist artist = new Artist();
        artist.setEmail(email);

        when(verificationService.validateArtistForVerification(email)).thenReturn(artist);
        when(imageServingUtil.saveImages(any(), anyString(), anyString(), anyBoolean()))
                .thenReturn("http://example.com/video.mp4");
        doThrow(new AppExceptions.DuplicateActionException("Duplicate verification request"))
                .when(verificationService).createVerification(eq(artist), anyString());

        mockMvc.perform(multipart("/api/verification/send")
                .file(video)
                .param("email", email)
                .param("lang", "en")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Duplicate verification request"));
    }

    @Test
    void testSendValidation_InternalServerError() throws Exception {
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "dummy".getBytes());
        String email = "artist@example.com";

        Artist artist = new Artist();
        artist.setEmail(email);

        when(verificationService.validateArtistForVerification(email)).thenReturn(artist);
        when(imageServingUtil.saveImages(any(), anyString(), anyString(), anyBoolean()))
                .thenReturn("http://example.com/video.mp4");
        doThrow(new AppExceptions.EmailSendException("Failed to send email"))
                .when(emailSenderService).sendVerificationEmail(artist);

        mockMvc.perform(multipart("/api/verification/send")
                .file(video)
                .param("email", email)
                .param("lang", "en")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to send email"))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}