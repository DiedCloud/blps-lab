package com.example.blps.service;

import com.example.blps.dao.repository.model.User;
import com.example.blps.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager manager;
    private final UserService userService;
    private final JWTService jwtService;
    private final SecurityConfig securityConfig;


    public String login(String username, String password) throws AuthenticationException {
        manager.authenticate(new UsernamePasswordAuthenticationToken(
                username, password
        ));

        User user = userService.getByLogin(username);

        return jwtService.generateToken(user);
    }


    public String register(String username, String password, String name) {
        User user = userService.createUser(
                username,
                securityConfig.passwordEncoder().encode(password),
                name
        );

        return jwtService.generateToken(user);
    }
}
