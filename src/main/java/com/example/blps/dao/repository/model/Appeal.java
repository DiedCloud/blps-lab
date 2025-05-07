package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private VideoInfo video;

    private String reason;
    private boolean processed;
}
