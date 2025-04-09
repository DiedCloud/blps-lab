package com.example.blps.dao.repository.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MonetizationInfo {
    @Id
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    @MapsId(value = "id")
    VideoInfo video;

    @Column(nullable = false)
    Float percent;
    @Column(nullable = false)
    Boolean isAgreed;
}
