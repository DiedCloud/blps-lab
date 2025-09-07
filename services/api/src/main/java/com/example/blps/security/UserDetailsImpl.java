package com.example.blps.security;

import com.example.blps.dao.repository.model.RolePermissions;
import com.example.blps.dao.repository.model.User;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public record UserDetailsImpl(User user) implements UserDetails {
    public UserDetailsImpl(@NonNull final User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> auth = new HashSet<>();
        user.getRoles().forEach(r -> auth.add(new SimpleGrantedAuthority("ROLE_" + r.getRole().getName())));
        user.getRoles().stream()
                .flatMap(r -> r.getRole().getPermissions().stream())
                .map(RolePermissions::getPermission)
                .forEach(p -> auth.add(new SimpleGrantedAuthority(p.getName())));
        return auth;
    }

    @Override
    public String getUsername() {
        return user.getLogin();
    }

    @Override
    @NonNull
    public String getPassword() {
        return user.getPassword();
    }
}
