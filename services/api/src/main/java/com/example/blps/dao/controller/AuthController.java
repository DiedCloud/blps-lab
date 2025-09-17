package com.example.blps.dao.controller;

import com.example.blps.dao.model.AuthRequestDTO;
import com.example.blps.dao.model.AuthResponseDTO;
import com.example.blps.dao.model.RegisterRequestDTO;
import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations for user authentication")
public class AuthController {
    private final AuthService authService;

    @CrossOrigin
    @PostMapping("/login")
    @Operation(summary = "Login to the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody final AuthRequestDTO request
    ) {

        try {
            final String jwt = authService.login(request.getLogin(), request.getPassword());

            return ResponseEntity.ok(
                    ResponseDTOs.ApiResponse.success(
                            new AuthResponseDTO(jwt),
                            "Login successful"
                    )
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(
                    ResponseDTOs.ApiResponse.error("Authentication failed: " + e.getMessage())
            );
        }
    }

    @CrossOrigin
    @PostMapping("/registration")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Registration failed")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<AuthResponseDTO>> registration(
            @Valid @RequestBody final RegisterRequestDTO request
    ) {

        final String jwt = authService.register(request.getLogin(), request.getPassword(), request.getName());

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(
                        new AuthResponseDTO(jwt),
                        "Registration successful"
                )
        );
    }
}