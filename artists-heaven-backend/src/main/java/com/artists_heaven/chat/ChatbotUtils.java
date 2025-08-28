package com.artists_heaven.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ChatbotUtils {

    static final Set<String> STOPWORDS_ES = loadStopwords("/stopwords_es.txt");
    static final Set<String> STOPWORDS_EN = loadStopwords("/stopwords_en.txt");

    private static final Set<String> DICTIONARY_ES = loadDictionary("/dictionary_es.txt");
    private static final Set<String> DICTIONARY_EN = loadDictionary("/dictionary_en.txt");

    private static final int MAX_LEVENSHTEIN_DISTANCE = 3;

    private static final LevenshteinDistance LD = new LevenshteinDistance(MAX_LEVENSHTEIN_DISTANCE);

    public static boolean isEnglish(String text) {
        String normalized = normalizeText(text.toLowerCase());
        String[] tokens = normalized.split("\\s+");

        long enCount = Arrays.stream(tokens).filter(STOPWORDS_EN::contains).count();
        long esCount = Arrays.stream(tokens).filter(STOPWORDS_ES::contains).count();

        // Si no detecta ninguna palabra en ninguna lista, devolver falso (no inglés)
        if (enCount == 0 && esCount == 0) {
            return false;
        }

        // Definimos un margen para evitar empates o falsos positivos
        int margin = 1;

        return enCount > esCount + margin;
    }

    public static String correctSpelling(String text, boolean isEnglish) {
        Pattern wordPattern = Pattern.compile("\\p{L}+");
        Matcher matcher = wordPattern.matcher(text);

        StringBuilder corrected = new StringBuilder();
        int lastEnd = 0;

        Set<String> dictionary = isEnglish ? DICTIONARY_EN : DICTIONARY_ES;

        while (matcher.find()) {
            corrected.append(text, lastEnd, matcher.start());

            String word = matcher.group().toLowerCase();
            String correctedWord = getClosestMatch(word, dictionary);
            corrected.append(correctedWord != null ? correctedWord : word);

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            corrected.append(text.substring(lastEnd));
        }

        return corrected.toString();
    }

    private static String getClosestMatch(String word, Set<String> dictionary) {
        Metaphone metaphone = new Metaphone();

        String wordCode = metaphone.encode(word);

        List<String> candidates = dictionary.stream()
                .filter(dictWord -> {
                    String dictCode = metaphone.encode(dictWord);
                    return dictCode.equals(wordCode);
                })
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            candidates = dictionary.stream()
                    .filter(dictWord -> Math.abs(dictWord.length() - word.length()) <= 2)
                    .collect(Collectors.toList());
        }

        // Busca mejor coincidencia en candidatos filtrados
        String bestMatch = null;
        int bestScore = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            int distance = LD.apply(word, candidate);
            if (distance > 3)
                continue;

            int lengthDiff = Math.abs(word.length() - candidate.length());
            int commonPrefix = commonPrefixLength(word, candidate);
            int commonChars = countCommonLetters(word, candidate);

            int score = (distance * 5) + (lengthDiff * 3) - (commonPrefix * 4) - (commonChars * 2);

            if (score < bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return (bestMatch != null && !bestMatch.equals(word)) ? bestMatch : null;
    }

    private static int commonPrefixLength(String a, String b) {
        int min = Math.min(a.length(), b.length());
        int i = 0;
        while (i < min && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private static int countCommonLetters(String a, String b) {
        Set<Character> setA = a.toLowerCase().chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        Set<Character> setB = b.toLowerCase().chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        setA.retainAll(setB);
        return setA.size();
    }

    public static String normalizeText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^\\p{L}\\p{Nd}\\s]", "");
    }

    public static Set<String> tokenize(String texto, boolean isEnglish) {
        texto = normalizeText(texto);

        Set<String> stopwords = isEnglish ? STOPWORDS_EN : STOPWORDS_ES;

        return Arrays.stream(texto.split("\\s+"))
                .filter(token -> !token.isBlank() && !stopwords.contains(token))
                .collect(Collectors.toSet());
    }

    public static double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private static Set<String> loadStopwords(String resourcePath) {
        return loadWordSet(resourcePath, "stopwords");
    }

    private static Set<String> loadDictionary(String resourcePath) {
        return loadWordSet(resourcePath, "dictionary").stream()
                .filter(word -> !word.contains(" "))
                .collect(Collectors.toSet());
    }

    private static Set<String> loadWordSet(String resourcePath, String label) {
        try {
            var stream = ChatbotUtils.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + label + " from " + resourcePath, e);
        }
    }
}
