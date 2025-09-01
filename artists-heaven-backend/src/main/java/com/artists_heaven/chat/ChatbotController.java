package com.artists_heaven.chat;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @Operation(summary = "Chat with Gemini chatbot", description = "Processes a user message and returns a response. " +
            "The flow is: 1) Detect predefined intent and return dynamic response, " +
            "2) Fallback to NLP predefined response, " +
            "3) If no response, check if API key is configured, " +
            "4) If configured, forward the message to Gemini API and return the answer.")
    @ApiResponse(responseCode = "200", description = "Chatbot successfully returned a response", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Hello! How can I help you today?\"}")))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g. empty message)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"The message cannot be empty\"}")))
    @ApiResponse(responseCode = "500", description = "Server error (e.g. API key missing or unexpected exception)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Internal server error\"}")))
    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> chatWithGemini(@Valid @RequestBody ChatMessageRequestDTO request) {
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
