package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table
public class Role {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "role", cascade = { CascadeType.ALL }, orphanRemoval = true)
    List<RolePermissions> permissions = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = { CascadeType.ALL }, orphanRemoval = true)
    List<UserRoles> users = new ArrayList<>();
}
