package com.example.blps.entity;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class Comment {
    @NonNull
    Long id;
    @NonNull
    User author;
    @NonNull
    String comment;
    @NonNull
    LocalDateTime published;
}
