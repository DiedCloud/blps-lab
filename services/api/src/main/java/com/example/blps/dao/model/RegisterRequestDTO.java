package com.example.blps.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RegisterRequestDTO implements Serializable {
    String login;
    String password;
    String name;
}
