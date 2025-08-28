package com.artists_heaven.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public ChatbotService(QARepository qaRepository, ProductService productService,
            @Value("classpath:chatbot/system-prompt.txt") Resource systemPromptResource,
            @Value("${chatbot.similarity-threshold:0.4}") double similarityThreshold) {
        this.qaRepository = qaRepository;
        this.productService = productService;
        this.systemPromptResource = systemPromptResource;
        this.similarityThreshold = similarityThreshold;
    }

    public String searchNLPAnswer(String text) {
        if (text == null || text.isBlank())
            return null;

        ChatMessageContext context = new ChatMessageContext(text);
        Set<String> messageTokens = context.getTokens();
        String originalText = context.getOriginalText();

        Map<String, ChatMessageContext> predefinedQAContexts = qaRepository.getAllQAContexts();

        // Calculamos las similitudes
        List<Map.Entry<String, Double>> matches = predefinedQAContexts.entrySet().stream()
                .map(entry -> {
                    Set<String> questionTokens = entry.getValue().getTokens();
                    double similarity = ChatbotUtils.jaccardSimilarity(messageTokens, questionTokens);
                    return Map.entry(entry.getKey(), similarity);
                })
                .filter(e -> e.getValue() >= similarityThreshold)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return null;
        }

        // Obtenemos la máxima similitud
        double maxSim = matches.stream()
                .mapToDouble(Map.Entry::getValue)
                .max()
                .orElse(0.0);

        // Filtramos solo los que tienen esa máxima similitud
        List<String> bestMatches = matches.stream()
                .filter(e -> e.getValue() == maxSim)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Desempate: seleccionamos la que tenga longitud más parecida a la pregunta
        // original
        String selected = bestMatches.stream()
                .min(Comparator.comparingInt(q -> Math.abs(q.length() - originalText.length())))
                .orElse(bestMatches.get(0));

        return qaRepository.getAnswer(selected);
    }

    public String searchDynamicAnswer(String text) {
        if (text == null || text.isBlank())
            return null;

        ChatMessageContext context = new ChatMessageContext(text);

        String intent = detectIntent(context.getNormalizedText());
        if (intent == null) {
            return null;
        }

        return generateResponseForIntent(intent, context.getLanguage() == ChatMessageContext.Language.ENGLISH);

    }

    private String detectIntent(String message) {
        // Normalizamos el mensaje: minúsculas y sin tildes
        String cleanedMessage = normalizeText(message);

        // Definimos el mapa de intenciones con sinónimos ampliados
        Map<String, List<String>> intents = Map.of(
                "recommendation", List.of(
                        // Español
                        "recomiendame", "me recomiendas", "sugerencia", "que me aconsejas", "quiero una recomendacion",
                        "aconsejame", "sugiere algo", "dame una sugerencia", "que me puedes sugerir",
                        "que me recomiendas", "tienes alguna sugerencia", "que producto me sugieres",
                        "cual me recomiendas", "me ayudas a elegir", "recomendacion de producto",
                        "me aconsejas algo", "dame ideas", "alguna idea", "que opcion me sugieres",
                        "elige por mi", "me das un consejo", "que deberia comprar",
                        // Inglés
                        "recommend me", "can you recommend", "suggestion", "what do you suggest",
                        "i want a recommendation",
                        "do you suggest", "suggest me something", "give me advice", "what should i buy",
                        "any recommendations", "which one do you recommend", "recommendation please",
                        "can you help me choose", "help me decide", "what do you advise",
                        "pick something for me", "what should i pick", "give me some ideas"),
                "bestseller", List.of(
                        // Español
                        "mas vendido", "top ventas", "producto popular", "lo que mas se vende", "mas comprado",
                        "producto mas vendido", "ventas altas", "mas pedido", "producto famoso", "producto estrella",
                        "cual se vende mas", "que compran mas", "producto mas comprado", "lo mas popular",
                        "lo mas pedido", "articulo mas vendido", "mas solicitado",
                        // Inglés
                        "best seller", "top selling", "popular product", "most sold", "best selling product",
                        "best product",
                        "most purchased", "most ordered", "top product", "hot item", "what sells the most",
                        "trending product", "people are buying", "most popular", "most requested",
                        "most wanted", "customer favorite"));

        // Recorremos intenciones y verificamos coincidencias
        for (Map.Entry<String, List<String>> entry : intents.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (cleanedMessage.contains(normalizeText(keyword))) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * Normaliza texto: minúsculas, elimina tildes y caracteres especiales.
     */
    private String normalizeText(String text) {
        if (text == null)
            return "";
        return text.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[^a-z0-9 ]", ""); // elimina caracteres no alfanuméricos
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
                    String name = sanitize(entry.getKey());
                    String details = sanitize(entry.getValue());
                    message.append("- ").append(name).append(": ").append(details).append("\n");
                }
                message.append("Check them out and let us know which one you like!");
            } else {
                message.append("Estos son algunos de nuestros productos más recomendados:\n");
                for (Map.Entry<String, String> entry : recommendedProducts.entrySet()) {
                    String name = sanitize(entry.getKey());
                    String details = sanitize(entry.getValue());
                    message.append("- ").append(name).append(": ").append(details).append("\n");
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
            String name = sanitize(topProduct.getOrDefault("nombre", "Unknown product"));
            String price = sanitize(topProduct.getOrDefault("precio", "price not available"));

            return isEnglish
                    ? "Our current best-selling product is '" + name + "'. Final price: €" + price
                            + ". Take a look and see what you think!"
                    : "El producto más vendido actualmente es '" + name + "'. Precio final: " + price
                            + "€. ¡Échale un vistazo y a ver qué te parece!";
        } else {
            return isEnglish
                    ? "Sorry, I don’t have information about our best-selling product at the moment."
                    : "Lo siento, no tengo información sobre el producto más vendido en este momento.";
        }
    }

    // Método para sanitizar (ejemplo básico)
    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("%", "%%"); // Escapa % para evitar interpretaciones
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
