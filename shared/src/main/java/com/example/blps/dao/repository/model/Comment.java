package com.example.blps.dao.repository.model;

import com.example.blps.dao.OwnedObject;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
public class Comment implements Serializable, OwnedObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(nullable = false)
    String content;
    @Column(nullable = false)
    LocalDateTime published;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    VideoInfo video;

    @Override
    public User getOwner() {
        return author;
    }
}
