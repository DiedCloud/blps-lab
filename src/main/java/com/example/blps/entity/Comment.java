package com.example.blps.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Comment {
    Long id;
    @NonNull
    User author;
    @NonNull
    String comment;
    @NonNull
    LocalDateTime published;
}
