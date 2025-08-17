package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "permission", cascade = { CascadeType.ALL }, orphanRemoval = true)
    List<RolePermissions> roles = new ArrayList<>();
}