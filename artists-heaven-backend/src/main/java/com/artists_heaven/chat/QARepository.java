package com.artists_heaven.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class QARepository {
    private Map<String, String> predefinedQA;

    public QARepository() {
        loadQAFromFile();
    }

    protected InputStream getResourceAsStream() {
        return getClass().getClassLoader().getResourceAsStream("predefinedQA.json");
    }

    private void loadQAFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getResourceAsStream()) {
            if (is == null) {
                throw new RuntimeException("No se encontró el archivo predefinedQA.json en resources");
            }
            predefinedQA = mapper.readValue(is, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando predefinedQA.json", e);
        }
    }

    public String getAnswer(String question) {
        if (question == null)
            return null;
        return predefinedQA.get(question.toLowerCase());
    }

    public Map<String, String> getAllQA() {
        return predefinedQA;
    }
}
