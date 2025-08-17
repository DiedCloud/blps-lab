package com.example.blps.dao.repository.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Data
@Table(name = "Client")
public class User implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> auth = new HashSet<>();
        this.getRoles().forEach(r -> auth.add(new SimpleGrantedAuthority("ROLE_" + r.role.getName())));
        this.getRoles().stream()
                .flatMap(r -> r.role.getPermissions().stream())
                .map(RolePermissions::getPermission)
                .forEach(p -> auth.add(new SimpleGrantedAuthority(p.getName())));
        return auth;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override @NonNull
    public String getPassword() {
        return password;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
