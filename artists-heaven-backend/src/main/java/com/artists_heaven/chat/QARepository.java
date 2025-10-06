package com.artists_heaven.chat;

import com.artists_heaven.exception.AppExceptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class QARepository {
    private Map<String, String> predefinedQA;
    private Map<String, ChatMessageContext> predefinedContexts;

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
                throw new AppExceptions.InternalServerErrorException("No se encontró el archivo predefinedQA.json en resources");
            }
            predefinedQA = mapper.readValue(is, Map.class);

            // Preprocesar contextos solo una vez
            predefinedContexts = new HashMap<>();
            for (String question : predefinedQA.keySet()) {
                ChatMessageContext ctx = new ChatMessageContext(question, true); // saltamos corrector
                predefinedContexts.put(question, ctx);
            }

        } catch (IOException e) {
            throw new AppExceptions.InternalServerErrorException("Error cargando predefinedQA.json");
        }
    }

    public String getAnswer(String question) {
        if (question == null)
            return null;
        return predefinedQA.get(question);
    }

    public Map<String, String> getAllQA() {
        return predefinedQA;
    }

    public Map<String, ChatMessageContext> getAllQAContexts() {
        return predefinedContexts;
    }
}

