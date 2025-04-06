package com.example.blps.entity;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class VideoInfo {
    @NonNull
    Long id;
    @NonNull
    String title;
    @NonNull
    String description;
    @NonNull
    LocalDateTime published;
    @NonNull
    User author;
}
