package com.example.blps.dao.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveApiResponseDTO(
        @JsonProperty("task_id") String taskId,
        String model,
        String version,
        List<HiveOutput> output
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HiveOutput(
            List<HiveClass> classes,
            @JsonProperty("string_matches") List<StringMatch> stringMatches
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HiveClass(
            @JsonProperty("class") String clazz,
            int value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StringMatch(
            String value,
            @JsonProperty("start_index") int startIndex,
            @JsonProperty("end_index") int endIndex,
            String type,
            Map<String, Object> extra
    ) {}
}