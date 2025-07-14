package com.artists_heaven.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.artists_heaven.chat.ChatbotController;
import com.artists_heaven.chat.ChatbotService;

class ChatbotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatbotController chatbotController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChatWithGemini_MessageIsNull() {
        Map<String, String> payload = Map.of();

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        // Por defecto isEnglish será true porque mensaje es null
        assertEquals("The message cannot be empty", body.get("reply"));
    }

    @Test
    void testChatWithGemini_NlpResponseFound() {
        Map<String, String> payload = Map.of("message", "Hi there!");

        when(chatbotService.searchNLPAnswer("Hi there!")).thenReturn("Predefined answer");

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Predefined answer", body.get("reply"));
    }

    @Test
    void testChatWithGemini_DynamicResponseFound() {
        Map<String, String> payload = Map.of("message", "Recommend me something");

        when(chatbotService.searchNLPAnswer(anyString())).thenReturn(null);
        when(chatbotService.searchDynamicAnswer(anyString())).thenReturn("Dynamic answer");

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Dynamic answer", body.get("reply"));
    }

    @Test
    void testChatWithGemini_ApiKeyNotConfigured() {
        Map<String, String> payload = Map.of("message", "Tell me more");

        when(chatbotService.searchNLPAnswer(anyString())).thenReturn(null);
        when(chatbotService.searchDynamicAnswer(anyString())).thenReturn(null);
        when(chatbotService.isApiKeyConfigured()).thenReturn(false);

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("API key not configured", body.get("reply"));
    }

    @Test
    void testChatWithGemini_CallsGeminiApi() throws Exception {
        Map<String, String> payload = Map.of("message", "Hello Gemini!");

        when(chatbotService.searchNLPAnswer(anyString())).thenReturn(null);
        when(chatbotService.searchDynamicAnswer(anyString())).thenReturn(null);
        when(chatbotService.isApiKeyConfigured()).thenReturn(true);
        when(chatbotService.callGeminiAPI("Hello Gemini!")).thenReturn("Gemini response");

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Gemini response", body.get("reply"));
    }

    @Test
    void testChatWithGemini_ExceptionThrown() throws Exception {
        Map<String, String> payload = Map.of("message", "My message contains a Trigger exception content");

        when(chatbotService.searchNLPAnswer(anyString())).thenReturn(null);
        when(chatbotService.searchDynamicAnswer(anyString())).thenReturn(null);
        when(chatbotService.isApiKeyConfigured()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(chatbotService).callGeminiAPI(anyString());

        ResponseEntity<?> response = chatbotController.chatWithGemini(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Internal server error", body.get("reply"));
    }
}
