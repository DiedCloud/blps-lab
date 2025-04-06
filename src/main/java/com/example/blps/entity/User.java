package com.example.blps.entity;

import lombok.Data;
import lombok.NonNull;

@Data
public class User {
    @NonNull
    Long id;
    @NonNull
    String login;
    @NonNull
    String name;
    @NonNull
    String password;
}
