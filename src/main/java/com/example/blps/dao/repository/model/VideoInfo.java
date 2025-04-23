package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class VideoInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    String description;
    @Column(nullable = false, name = "transcription_key")
    String transcriptionKey;
    @Column(nullable = false, name = "storage_key")
    String storageKey;
    @Column(nullable = false)
    LocalDateTime published;

    @Enumerated(EnumType.STRING)
    private MonetizationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;
}
