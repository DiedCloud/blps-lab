package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class UserMapper {
    static public com.example.blps.entity.User getUser(
            @NonNull com.example.blps.dao.repository.model.User user
    ) {
        return new com.example.blps.entity.User(
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getPassword()
        );
    }
}
