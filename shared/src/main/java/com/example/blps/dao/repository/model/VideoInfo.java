package com.example.blps.dao.repository.model;

import com.example.blps.dao.OwnedObject;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class VideoInfo implements Serializable, OwnedObject {
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
    @Column(nullable = false, name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    private MonetizationStatus status = MonetizationStatus.PROCESSING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @OneToMany(mappedBy = "video", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> comments;

    @Override
    public User getOwner() {
        return author;
    }
}
