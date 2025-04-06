package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.AuthRequestDTO;
import com.example.blps.dao.controller.model.AuthResponseDTO;
import com.example.blps.dao.controller.model.RegisterRequestDTO;
import com.example.blps.entity.User;
import com.example.blps.service.JWTService;
import com.example.blps.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthenticationManager manager;
    private final UserService userService;
    private final JWTService jwtService;

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody final AuthRequestDTO request) {
        manager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(), request.getPassword()
        ));
        User user = userService.getByLogin(request.getLogin());
        final String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwt));
    }

    @CrossOrigin
    @PostMapping("/registration")
    public ResponseEntity<AuthResponseDTO> registration(@RequestBody final RegisterRequestDTO request) {
        User user = userService.createUser(request.getLogin(), request.getPassword(), request.getName());
        final String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwt));
    }
}
