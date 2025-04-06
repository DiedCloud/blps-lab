package com.example.blps.dao.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AuthRequestDTO implements Serializable {
    String login;
    String password;
}
