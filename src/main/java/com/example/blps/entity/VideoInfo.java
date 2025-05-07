package com.example.blps.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class VideoInfo {
    Long id;
    @NonNull
    String title;
    @NonNull
    String description;
    @NonNull
    String transcriptionKey;
    @NonNull
    String storageKey;
    @NonNull
    LocalDateTime published;
    @NonNull
    User author;
}
