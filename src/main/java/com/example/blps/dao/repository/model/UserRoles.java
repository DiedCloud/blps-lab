package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
class UserRoleId implements Serializable {
    @Column(name = "user_id", nullable = false)
    Long idUser = 0L;
    @Column(name = "role_id", nullable = false)
    Long idRole = 0L;
}

@Entity
@Data
@Table
public class UserRoles {
    @EmbeddedId
    UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUser")
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRole")
    @JoinColumn(name = "role_id", nullable = false)
    Role role;
}