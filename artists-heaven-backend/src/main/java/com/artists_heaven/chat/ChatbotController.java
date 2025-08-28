package com.artists_heaven.chat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<?> chatWithGemini(@Valid @RequestBody ChatMessageRequestDTO request) {
        try {
            String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";

            if (userMessage.isBlank()) {
                return badRequest(true,
                        "El mensaje no puede estar vacío",
                        "The message cannot be empty");
            }

            boolean isEnglish = ChatbotUtils.isEnglish(userMessage);

            // 1. Dynamic Response (Intent Detection first)
            String dynamicResponse = chatbotService.searchDynamicAnswer(userMessage);
            if (dynamicResponse != null) {
                return ok(dynamicResponse);
            }

            // 2. Predefined NLP Response (only if no intent detected)
            String predefinedResponse = chatbotService.searchNLPAnswer(userMessage);
            if (predefinedResponse != null) {
                return ok(predefinedResponse);
            }

            // 3. Check API Key
            if (!chatbotService.isApiKeyConfigured()) {
                return serverError(isEnglish,
                        "API key no configurada",
                        "API key not configured");
            }

            // 4. Query Gemini API
            String geminiResponse = chatbotService.callGeminiAPI(userMessage);
            return ok(geminiResponse);

        } catch (Exception e) {
            return serverError(true,
                    "Error interno en el servidor",
                    "Internal server error");
        }
    }

    private ResponseEntity<Map<String, String>> ok(String reply) {
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    private ResponseEntity<Map<String, String>> badRequest(boolean isEnglish, String messageEs, String messageEn) {
        String message = isEnglish ? messageEn : messageEs;
        return ResponseEntity.badRequest().body(Map.of("reply", message));
    }

    private ResponseEntity<Map<String, String>> serverError(boolean isEnglish, String messageEs, String messageEn) {
        String message = isEnglish ? messageEn : messageEs;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("reply", message));
    }
}
