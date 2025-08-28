package com.example.blps.infra.transcription;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProfanityFilter {

    private final Set<String> badWords = new HashSet<>();
    private final Pattern wordPattern = Pattern.compile("\\b(\\w+)\\b", Pattern.UNICODE_CHARACTER_CLASS);

    public ProfanityFilter(@Value("${whisper.banned_words_path}") String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip().toLowerCase();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        badWords.add(line);
                    }
                }
            }
        }
    }

    public boolean containsBadWords(String text) {
        Matcher matcher = wordPattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            if (badWords.contains(matcher.group())) {
                return true;
            }
        }
        return false;
    }

    public String censorText(String text) {
        Matcher matcher = wordPattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String word = matcher.group();
            if (badWords.contains(word.toLowerCase())) {
                matcher.appendReplacement(sb, "*".repeat(word.length()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
