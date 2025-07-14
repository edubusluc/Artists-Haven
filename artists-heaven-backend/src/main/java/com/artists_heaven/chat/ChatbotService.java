package com.artists_heaven.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;

import com.artists_heaven.product.ProductService;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class ChatbotService {

    private final ProductService productService;
    private final QARepository qaRepository;
    private final Dotenv dotenv = Dotenv.load();

    private final String GEMINI_API_KEY = dotenv.get("GEMINI_KEY");
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final double similarityThreshold;

    private Resource systemPromptResource;

    private String systemPromptText;

    @PostConstruct
    public void loadSystemPrompt() throws IOException {
        try (InputStream is = systemPromptResource.getInputStream()) {
            this.systemPromptText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    public ChatbotService(QARepository qaRepository, ProductService productService, @Value("classpath:chatbot/system-prompt.txt") Resource systemPromptResource,
            @Value("${chatbot.similarity-threshold:0.4}") double similarityThreshold) {
        this.qaRepository = qaRepository;
        this.productService = productService;
        this.systemPromptResource = systemPromptResource;
        this.similarityThreshold = similarityThreshold;
    }

    private String processMessage(String text) {
        boolean isEnglish = ChatbotUtils.isEnglish(text);
        String messageCorrected = ChatbotUtils.correctSpelling(text.toLowerCase(), isEnglish);
        String messageClean = ChatbotUtils.normalizeText(messageCorrected).trim();
        return messageClean;
    }

    public String searchNLPAnswer(String text) {
        if (text == null || text.isBlank())
            return null;

        boolean isEnglish = ChatbotUtils.isEnglish(text);
        String cleanedMessage = processMessage(text);
        Set<String> messageTokens = ChatbotUtils.tokenize(cleanedMessage, isEnglish);
        Map<String, String> predefinedQA = qaRepository.getAllQA();

        return predefinedQA.entrySet().stream()
                .map(entry -> {
                    boolean entryIsEnglish = ChatbotUtils.isEnglish(entry.getKey());
                    Set<String> questionTokens = ChatbotUtils.tokenize(entry.getKey(), entryIsEnglish);
                    double similarity = ChatbotUtils.jaccardSimilarity(messageTokens, questionTokens);
                    return Map.entry(entry.getValue(), similarity);
                })
                .filter(e -> e.getValue() >= similarityThreshold)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String searchDynamicAnswer(String text) {
        if (text == null || text.isBlank())
            return null;

        boolean isEnglish = ChatbotUtils.isEnglish(text);
        String cleanedMessage = processMessage(text);

        String intent = detectIntent(cleanedMessage);
        if (intent == null) {
            return null;
        }

        return generateResponseForIntent(intent, isEnglish);
    }

    private String detectIntent(String cleanedMessage) {
        Map<String, List<String>> intents = Map.of(
                "recommendation", List.of(
                        "recomiendame", "me recomiendas", "sugerencia", "que me aconsejas", "quiero una recomendacion",
                        "recommend me", "can you recommend", "suggestion", "what do you suggest",
                        "i want a recommendation"),
                "bestseller", List.of(
                        "mas vendido", "top ventas", "producto popular", "lo que mas se vende", "mas comprado",
                        "best seller", "top selling", "popular product", "most sold", "best selling product"));

        for (Map.Entry<String, List<String>> entry : intents.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (cleanedMessage.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String generateResponseForIntent(String intent, boolean isEnglish) {
        switch (intent) {
            case "recommendation":
                return getRecommendedProductMessage(isEnglish);
            case "bestseller":
                return getTopSellingProductMessage(isEnglish);
            default:
                return null;
        }
    }

    private String getRecommendedProductMessage(boolean isEnglish) {
        Map<String, String> recommendedProducts = productService.getRecommendedProduct();
        if (recommendedProducts != null && !recommendedProducts.isEmpty()) {
            StringBuilder message = new StringBuilder();

            if (isEnglish) {
                message.append("Here are some of our top recommended products:\n");
                for (Map.Entry<String, String> entry : recommendedProducts.entrySet()) {
                    String name = entry.getKey();
                    String details = entry.getValue();
                    message.append(String.format("- %s: %s\n", name, details));
                }
                message.append("Check them out and let us know which one you like!");
            } else {
                message.append("Estos son algunos de nuestros productos más recomendados:\n");
                for (Map.Entry<String, String> entry : recommendedProducts.entrySet()) {
                    String name = entry.getKey();
                    String details = entry.getValue();
                    message.append(String.format("- %s: %s\n", name, details));
                }
                message.append("¡Échales un vistazo y cuéntanos cuál te gusta más!");
            }

            return message.toString();
        } else {
            return isEnglish
                    ? "Sorry, I don’t have any recommended products to show at the moment."
                    : "Lo siento, no tengo productos recomendados para mostrar en este momento.";
        }
    }

    private String getTopSellingProductMessage(boolean isEnglish) {
        Map<String, String> topProduct = productService.getTopSellingProduct();
        if (topProduct != null && !topProduct.isEmpty()) {
            String name = topProduct.getOrDefault("nombre", "Unknown product");
            String price = topProduct.getOrDefault("precio", "price not available");

            return isEnglish
                    ? String.format(
                            "Our current best-selling product is '%s'. Final price: €%s. Take a look and see what you think!",
                            name, price)
                    : String.format(
                            "El producto más vendido actualmente es '%s'. Precio final: %s€. ¡Échale un vistazo y a ver qué te parece!",
                            name, price);
        } else {
            return isEnglish
                    ? "Sorry, I don’t have information about our best-selling product at the moment."
                    : "Lo siento, no tengo información sobre el producto más vendido en este momento.";
        }
    }

    public boolean isApiKeyConfigured() {
        return GEMINI_API_KEY != null && !GEMINI_API_KEY.isBlank();
    }

    public String callGeminiAPI(String userMessage) throws Exception {
        RestTemplate restTemplate = createRestTemplate();

        Map<String, Object> systemMessage = Map.of("text", systemPromptText);

        Map<String, Object> userMessageMap = Map.of("text", userMessage);
        Map<String, Object> content1 = Map.of("role", "user", "parts", List.of(systemMessage));
        Map<String, Object> content2 = Map.of("role", "user", "parts", List.of(userMessageMap));
        Map<String, Object> requestBody = Map.of("contents", List.of(content1, content2));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", GEMINI_API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error del servidor de Gemini");
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("candidates")) {
            throw new RuntimeException("Respuesta inesperada de Gemini");
        }

        var candidatesList = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidatesList.isEmpty()) {
            throw new RuntimeException("No se generó respuesta de Gemini");
        }

        Map<String, Object> firstCandidate = candidatesList.get(0);
        Map<String, Object> contentResponse = (Map<String, Object>) firstCandidate.get("content");
        var parts = (List<Map<String, Object>>) contentResponse.get("parts");

        if (parts.isEmpty()) {
            throw new RuntimeException("Respuesta sin partes de texto");
        }

        return (String) parts.get(0).get("text");
    }
}
