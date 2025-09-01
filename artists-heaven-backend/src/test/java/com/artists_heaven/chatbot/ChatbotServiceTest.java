package com.artists_heaven.chatbot;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.artists_heaven.chat.ChatMessageContext;
import com.artists_heaven.chat.ChatbotService;
import com.artists_heaven.chat.QARepository;
import com.artists_heaven.product.ProductService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class ChatbotServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private QARepository qaRepository;

    @Mock
    private Resource systemPromptResource;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Set up manual injection
        chatbotService = new ChatbotService(
                qaRepository,
                productService,
                systemPromptResource,
                0.4);

        // Mock system prompt file content
        String mockPrompt = "This is a system prompt.";
        when(systemPromptResource.getInputStream()).thenReturn(
                new ByteArrayInputStream(mockPrompt.getBytes(StandardCharsets.UTF_8)));

        chatbotService.loadSystemPrompt();
    }

    @Test
    void testSearchNLPAnswer_FindsSimilarAnswer() {
        // Arrange
        String question = "Can you recommend me something?";

        // La clave debe ser la pregunta que vamos a comparar
        String predefinedQuestion = "recommend me something";
        ChatMessageContext context = new ChatMessageContext(predefinedQuestion, true);

        Map<String, ChatMessageContext> predefinedQA = Map.of(
                predefinedQuestion, context);

        when(qaRepository.getAllQAContexts()).thenReturn(predefinedQA);
        when(qaRepository.getAnswer(predefinedQuestion)).thenReturn("Sure, I recommend our best products!");

        // Act
        String answer = chatbotService.searchNLPAnswer(question);

        // Assert
        assertNotNull(answer);
        assertTrue(answer.contains("recommend"));
    }

    @Test
    void testSearchNLPAnswer_NoSimilarAnswer() {

        String question = "abrakadabra completelyunrelated";
        Map<String, String> predefinedQA = Map.of(
                "how are you", "I’m fine thanks!");
        when(qaRepository.getAllQA()).thenReturn(predefinedQA);

        String answer = chatbotService.searchNLPAnswer(question);

        assertNull(answer);
    }

    @Test
    void testSearchNLPAnswer_NullText_ReturnsNull() {
        // Act
        String answer = chatbotService.searchNLPAnswer(null);

        // Assert
        assertNull(answer);
    }

    @Test
    void testSearchDynamicAnswer_RecommendationIntent_English() {
        // Arrange
        when(productService.getRecommendedProduct()).thenReturn(Map.of(
                "Canvas Art", "A beautiful canvas painting"));

        String text = "Can you recommend me something?";

        // Act
        String result = chatbotService.searchDynamicAnswer(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("recommended products"));
        assertTrue(result.contains("Canvas Art"));
    }

    @Test
    void testSearchDynamicAnswer_RecommendationIntent_Spanish() {
        // Arrange
        when(productService.getRecommendedProduct()).thenReturn(Map.of(
                "Camiseta de Test", "Una camiseta recomendada en el test"));

        String text = "¿Me recomiendas algún producto?";

        // Act
        String result = chatbotService.searchDynamicAnswer(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Camiseta de Test"));
        assertTrue(result.contains("Una camiseta recomendada en el test"));
    }

    @Test
    void testSearchDynamicAnswer_NotRecommendationAvailable_English() {
        // Arrange
        when(productService.getRecommendedProduct()).thenReturn(null);

        String text = "Can you recommend me something?";

        // Act
        String result = chatbotService.searchDynamicAnswer(text);

        // Assert
        assertTrue(result.contains("Sorry, I don’t have any recommended products to show at the moment."));
    }

    @Test
    void testSearchDynamicAnswer_NotRecommendationAvailable_Spanish() {
        // Arrange
        when(productService.getRecommendedProduct()).thenReturn(null);

        String text = "¿Me recomiendas algún producto?";

        // Act
        String result = chatbotService.searchDynamicAnswer(text);

        // Assert
        assertTrue(result.contains("Lo siento, no tengo productos recomendados para mostrar en este momento."));
    }

    @Test
    void testSearchDynamicAnswer_BestsellerIntentNull_English() {
        when(productService.getTopSellingProduct()).thenReturn(null);

        String text = "What is the best seller product?";

        String result = chatbotService.searchDynamicAnswer(text);

        assertNotNull(result);
        assertTrue(result.contains("Sorry, I don’t have information"));

    }

    @Test
    void testSearchDynamicAnswer_BestsellerIntentNull_Spanish() {
        when(productService.getTopSellingProduct()).thenReturn(null);

        String text = "¿Que producto es el más vendido?";

        String result = chatbotService.searchDynamicAnswer(text);

        assertNotNull(result);
        assertTrue(result.contains("Lo siento, no tengo información"));

    }

    @Test
    void testSearchDynamicAnswer_BestsellerIntent_English() {
        when(productService.getTopSellingProduct()).thenReturn(Map.of(
                "nombre", "Summer Tshirt",
                "precio", "59.99"));

        String text = "What is the best seller product?";

        String result = chatbotService.searchDynamicAnswer(text);

        assertNotNull(result);
        assertTrue(result.contains("Our current best-selling product is"));
        assertTrue(result.contains("Summer Tshirt"));
    }

    @Test
    void testSearchDynamicAnswer_BestsellerIntent_Spanish() {
        when(productService.getTopSellingProduct()).thenReturn(Map.of(
                "nombre", "Pintura Abstracta",
                "precio", "59.99"));

        String text = "¿Cuál es el producto más vendido?";

        String result = chatbotService.searchDynamicAnswer(text);

        assertNotNull(result);
        assertTrue(result.contains("El producto más vendido actualmente es"));
        assertTrue(result.contains("Pintura Abstracta"));
    }

    @Test
    void testSearchDynamicAnswer_NoIntentDetected() {
        String text = "Esto no coincide con ningún intento conocido.";

        String result = chatbotService.searchDynamicAnswer(text);

        assertNull(result);
    }

    @Test
    void testSearchDynamicAnswer_NullText_ReturnsNull() {
        // Act
        String answer = chatbotService.searchDynamicAnswer(null);

        // Assert
        assertNull(answer);
    }

    @Test
    void testIsApiKeyConfigured_ReturnsTrue() {
        assertTrue(chatbotService.isApiKeyConfigured());
    }

    @Test
    void testCallGeminiAPI_ReturnsGeneratedText() throws Exception {
        // Mock RestTemplate
        RestTemplate restTemplate = mock(RestTemplate.class);
        ChatbotService spyService = spy(chatbotService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        // Mock response body
        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of(
                                "content", Map.of(
                                        "parts", List.of(
                                                Map.of("text", "Generated response from Gemini."))))));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        String userMessage = "Hello, Gemini!";
        String response = spyService.callGeminiAPI(userMessage);

        assertEquals("Generated response from Gemini.", response);
    }

    @Test
    void testCallGeminiAPI_ThrowsOnErrorResponse() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ChatbotService spyService = spy(chatbotService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            spyService.callGeminiAPI("Hello!");
        });

        assertTrue(exception.getMessage().contains("Error del servidor de Gemini"));
    }

    @Test
    void testCallGeminiAPI_ThrowsWhenResponseBodyIsNull() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ChatbotService spyService = spy(chatbotService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            spyService.callGeminiAPI("Hola");
        });

        assertTrue(exception.getMessage().contains("Respuesta inesperada de Gemini"));
    }

    @Test
    void testCallGeminiAPI_ThrowsWhenCandidatesIsEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ChatbotService spyService = spy(chatbotService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        Map<String, Object> responseBody = Map.of("candidates", List.of());
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            spyService.callGeminiAPI("Hola");
        });

        assertTrue(exception.getMessage().contains("No se generó respuesta de Gemini"));
    }

    @Test
    void testCallGeminiAPI_ThrowsWhenPartsIsEmpty() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ChatbotService spyService = spy(chatbotService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of("parts", List.of())) // 👈 parts vacío
                ));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            spyService.callGeminiAPI("Hola");
        });

        assertTrue(exception.getMessage().contains("Respuesta sin partes de texto"));
    }

}
