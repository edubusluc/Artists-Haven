package com.artists_heaven.chatbot;

import static org.junit.jupiter.api.Assertions.*;

import com.artists_heaven.chat.QARepository;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

class QARepositoryTest {

    @Test
    void testLoadQAFromFile_LoadsSuccessfully() throws Exception {
        // Arrange
        String json = "{ \"hello\": \"Hi there!\", \"bye\": \"Goodbye!\" }";
        InputStream is = new ByteArrayInputStream(json.getBytes());

        QARepository repo = new QARepository() {
            @Override
            protected InputStream getResourceAsStream() {
                return is;
            }
        };

        // Act
        Map<String, String> qa = repo.getAllQA();

        // Assert
        assertNotNull(qa);
        assertEquals("Hi there!", qa.get("hello"));
        assertEquals("Goodbye!", qa.get("bye"));
    }

    @Test
    void testGetAnswer_ReturnsCorrectAnswer() throws Exception {
        String json = "{ \"hello\": \"Hi there!\" }";
        InputStream is = new ByteArrayInputStream(json.getBytes());

        QARepository repo = new QARepository() {
            @Override
            protected InputStream getResourceAsStream() {
                return is;
            }
        };

        assertEquals("Hi there!", repo.getAnswer("hello"));
        assertEquals("Hi there!", repo.getAnswer("HELLO")); // test case-insensitive
        assertNull(repo.getAnswer("unknown"));
        assertNull(repo.getAnswer(null));
    }

    @Test
    void testLoadQAFromFile_ThrowsWhenFileMissing() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            new QARepository() {
                @Override
                protected InputStream getResourceAsStream() {
                    return null; // Simulate missing file
                }
            };
        });
        assertTrue(ex.getMessage().contains("No se encontró"));
    }

    @Test
    void testLoadQAFromFile_ThrowsOnInvalidJson() {
        String invalidJson = "Not a JSON";
        InputStream is = new ByteArrayInputStream(invalidJson.getBytes());

        Exception ex = assertThrows(RuntimeException.class, () -> {
            new QARepository() {
                @Override
                protected InputStream getResourceAsStream() {
                    return is;
                }
            };
        });

        assertTrue(ex.getMessage().contains("Error cargando"));
    }
}
