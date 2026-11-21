package com.campusfind.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementasi {@link MatchingStrategy} berbasis token-overlap.
 * <p>
 * Kedua string dipecah menjadi token alfanumerik, lalu dihitung
 * persentase token ciri-rahasia yang muncul di ciri-khusus.
 */
public class TokenOverlapStrategy implements MatchingStrategy {

    @Override
    public int skor(String ciriRahasia, String ciriKhusus) {
        if (ciriRahasia == null || ciriRahasia.isBlank()) {
            return 0;
        }

        Set<String> rahasiaTokens = tokenize(ciriRahasia);
        if (rahasiaTokens.isEmpty()) {
            return 0;
        }

        Set<String> khususTokens = tokenize(ciriKhusus);

        long intersectionCount = rahasiaTokens.stream()
                .filter(khususTokens::contains)
                .count();

        return (int) Math.round((double) intersectionCount / rahasiaTokens.size() * 100);
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return Arrays.stream(lower.split("[^a-z0-9]+"))
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
