package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Data
@Table(name = "Client")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String login;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    String password;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> comments;
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<VideoInfo> videos;

    @OneToMany(mappedBy = "user", cascade = { CascadeType.ALL }, orphanRemoval = true)
    List<UserRoles> roles = new ArrayList<>();
}
