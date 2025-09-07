package com.example.blps.dao.controller.model;

import java.util.List;

public record ModerationResultDTO(
        boolean approved,
        String reason,
        List<String> flaggedCategories,
        List<String> matches
) {
    public static ModerationResultDTO fromHive(HiveApiResponseDTO response) {
        HiveApiResponseDTO.HiveOutput output = response.output().get(0);

        // Собираем все классы с value > 0
        List<String> flagged = output.classes().stream()
                .filter(c -> c.value() > 0)
                .map(HiveApiResponseDTO.HiveClass::clazz)
                .toList();

        // Собираем совпадения (например profanity)
        List<String> matches = output.stringMatches() != null
                ? output.stringMatches().stream().map(HiveApiResponseDTO.StringMatch::value).toList()
                : List.of();

        boolean safe = flagged.isEmpty() && matches.isEmpty();

        return new ModerationResultDTO(
                safe,
                safe ? "Approved" : "Rejected",
                flagged,
                matches
        );
    }
}

