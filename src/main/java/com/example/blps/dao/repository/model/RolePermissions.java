package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
class RolePermissionId implements Serializable {
    @Column(name = "role_id", nullable = false)
    Long idRole = 0L;
    @Column(name = "permission_id", nullable = false)
    Long idPermission = 0L;
}

@Entity
@Data
@Table
public class RolePermissions {
    @EmbeddedId
    RolePermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRole")
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPermission")
    @JoinColumn(name = "permission_id", nullable = false)
    Permission permission;
}
