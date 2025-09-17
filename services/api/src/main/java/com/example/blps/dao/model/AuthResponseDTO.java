package com.example.blps.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AuthResponseDTO implements Serializable {
    String token;
}
