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

import com.artists_heaven.exception.AppExceptions;

public class ChatbotUtils {

    static final Set<String> STOPWORDS_ES = loadStopwords("/stopwords_es.txt");
    static final Set<String> STOPWORDS_EN = loadStopwords("/stopwords_en.txt");

    private static final Set<String> DICTIONARY_ES = loadDictionary("/dictionary_es.txt");
    private static final Set<String> DICTIONARY_EN = loadDictionary("/dictionary_en.txt");

    private static final int MAX_LEVENSHTEIN_DISTANCE = 3;

    private static final LevenshteinDistance LD = new LevenshteinDistance(MAX_LEVENSHTEIN_DISTANCE);

    /**
     * Detects if a given text is in English by comparing stopword frequency.
     *
     * @param text input text
     * @return {@code true} if text is more likely English, {@code false} otherwise
     */
    public static boolean isEnglish(String text) {
        String normalized = normalizeText(text.toLowerCase());
        String[] tokens = normalized.split("\\s+");

        long enCount = Arrays.stream(tokens).filter(STOPWORDS_EN::contains).count();
        long esCount = Arrays.stream(tokens).filter(STOPWORDS_ES::contains).count();

        if (enCount == 0 && esCount == 0) {
            return false;
        }
        int margin = 1;

        return enCount > esCount + margin;
    }

    /**
     * Corrects spelling of words in the given text using phonetic (Metaphone) and
     * Levenshtein distance.
     *
     * @param text      input text
     * @param isEnglish whether the text is in English (true) or Spanish (false)
     * @return corrected text with spelling fixes
     */
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

    /**
     * Finds the closest dictionary match for a given word using phonetic and
     * distance algorithms.
     *
     * @param word       word to check
     * @param dictionary language dictionary
     * @return best match or {@code null} if none found
     */
    private static String getClosestMatch(String word, Set<String> dictionary) {
        Metaphone metaphone = new Metaphone();

        String wordCode = metaphone.encode(word);

        List<String> candidates = dictionary.stream()
                .filter(dictWord -> {
                    String dictCode = metaphone.encode(dictWord);
                    return dictCode.equals(wordCode);
                })
                .toList();

        if (candidates.isEmpty()) {
            candidates = dictionary.stream()
                    .filter(dictWord -> Math.abs(dictWord.length() - word.length()) <= 2)
                    .toList();
        }

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

    /**
     * Calculates the length of the common prefix between two words.
     *
     * @param a first word
     * @param b second word
     * @return length of common prefix
     */
    private static int commonPrefixLength(String a, String b) {
        int min = Math.min(a.length(), b.length());
        int i = 0;
        while (i < min && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    /**
     * Counts the number of distinct common letters between two words.
     *
     * @param a first word
     * @param b second word
     * @return number of shared characters
     */
    private static int countCommonLetters(String a, String b) {
        Set<Character> setA = a.toLowerCase().chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        Set<Character> setB = b.toLowerCase().chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        setA.retainAll(setB);
        return setA.size();
    }

    /**
     * Normalizes text by removing diacritics and non-alphanumeric characters.
     *
     * @param text input text
     * @return normalized text
     */
    public static String normalizeText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^\\p{L}\\p{Nd}\\s]", "");
    }

    /**
     * Tokenizes text into a set of words, filtering out stopwords.
     *
     * @param texto     input text
     * @param isEnglish whether the text is English or Spanish
     * @return set of tokens without stopwords
     */
    public static Set<String> tokenize(String texto, boolean isEnglish) {
        texto = normalizeText(texto);

        Set<String> stopwords = isEnglish ? STOPWORDS_EN : STOPWORDS_ES;

        return Arrays.stream(texto.split("\\s+"))
                .filter(token -> !token.isBlank() && !stopwords.contains(token))
                .collect(Collectors.toSet());
    }

    /**
     * Calculates the Jaccard similarity between two sets of tokens.
     *
     * @param set1 first set of tokens
     * @param set2 second set of tokens
     * @return similarity value between 0.0 and 1.0
     */
    public static double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Loads stopwords from a resource file.
     *
     * @param resourcePath path to stopwords file
     * @return set of stopwords
     */
    private static Set<String> loadStopwords(String resourcePath) {
        return loadWordSet(resourcePath, "stopwords");
    }

    /**
     * Loads dictionary words from a resource file, excluding multi-word entries.
     *
     * @param resourcePath path to dictionary file
     * @return set of dictionary words
     */
    private static Set<String> loadDictionary(String resourcePath) {
        return loadWordSet(resourcePath, "dictionary").stream()
                .filter(word -> !word.contains(" "))
                .collect(Collectors.toSet());
    }

    /**
     * Loads a generic word set from a resource file.
     *
     * @param resourcePath path to resource file
     * @param label        descriptive label for error handling
     * @return set of words
     * @throws AppExceptions.InternalServerErrorException if resource cannot be
     *                                                    loaded
     */
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
        } catch (Exception e) {
            throw new AppExceptions.InternalServerErrorException("Error loading " + label + " from " + resourcePath);
        }
    }
}
