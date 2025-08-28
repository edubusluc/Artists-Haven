package com.artists_heaven.chat;

import java.util.Set;

public class ChatMessageContext {
    private final String firtsText;
    private final String normalizedText;
    private final Set<String> tokens;
    private final Language language;

    public enum Language {
        ENGLISH, SPANISH, UNKNOWN
    }

    public ChatMessageContext(String originalText) {
        this(originalText, false);
    }

public ChatMessageContext(String originalText, boolean skipSpellCheck) {
    String lowerText = originalText.toLowerCase();
    String normalized = ChatbotUtils.normalizeText(lowerText);

    boolean isEnglish = ChatbotUtils.isEnglish(normalized);
    String processedText = skipSpellCheck
            ? normalized
            : ChatbotUtils.correctSpelling(normalized, isEnglish);

    this.normalizedText = ChatbotUtils.normalizeText(processedText); 
    this.tokens = ChatbotUtils.tokenize(this.normalizedText, isEnglish);
    this.language = isEnglish ? Language.ENGLISH : Language.SPANISH;
    this.firtsText = originalText;
}


    public Language getLanguage() {
        return language;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public Set<String> getTokens() {
        return tokens;
    }

    public Boolean isEnglish() {
        return language == Language.ENGLISH;
    }

    public String getOriginalText() {
        return firtsText;
    }
}

