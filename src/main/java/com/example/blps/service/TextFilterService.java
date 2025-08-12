package com.example.blps.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class TextFilterService {

    private final Set<String> bannedPhrases = new HashSet<>();

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("banned_words.txt"),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    bannedPhrases.add(line.trim().toLowerCase());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load banned words list", e);
        }
    }

    public Boolean containsBannedWord(String text) {
        String normalizedText = text.toLowerCase();

        for (String phrase : bannedPhrases) {
            if (normalizedText.contains(phrase)) {
                return false;
            }
        }

        return true;
    }
}
