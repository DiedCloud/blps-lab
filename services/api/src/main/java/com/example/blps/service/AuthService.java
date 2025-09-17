package com.example.blps.service;

import com.example.blps.dao.repository.model.User;
import com.example.blps.security.JWTFilter;
import com.example.blps.security.PermissionEvaluatorImpl;
import com.example.blps.security.SecurityConfig;
import com.example.blps.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager manager;
    private final UserService userService;
    private final JWTService jwtService;
    private final JWTFilter jwtFilter;
    private final SecurityConfig securityConfig;

    private final PermissionEvaluatorImpl permissionEvaluator;


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

    public void checkTokenInDelegatedAuth(Long userId) throws IllegalAccessException {
        String token = TokenService.getUserToken(userId);
        if (token == null)
            throw new IllegalAccessException("Unauthorized");
        jwtFilter.doFilter(token);
    }

    public boolean hasPermissionInDelegatedAuth(DelegateExecution delegateExecution, Long targetId, String targetType, String authority) throws IllegalAccessException {
        String userId = delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId();

        checkTokenInDelegatedAuth(Long.valueOf(userId));

        User user = userService.getById(Long.valueOf(userId));

        return permissionEvaluator.hasPermission(
                new UsernamePasswordAuthenticationToken(user, null),
                targetId,
                targetType,
                authority
        );
    }

    public boolean hasNoAuthorityInDelegatedAuth(DelegateExecution delegateExecution, String... authorities) throws IllegalAccessException {
        String userId = delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId();

        checkTokenInDelegatedAuth(Long.valueOf(userId));

        UserDetailsImpl user = new UserDetailsImpl(userService.getById(Long.valueOf(userId)));

        return Collections.disjoint(user.getAuthorities(), List.of(authorities));
    }
}
