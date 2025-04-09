package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class UserMapper {
    static public com.example.blps.entity.User getUser(
            @NonNull com.example.blps.dao.repository.model.User user
    ) {
        return new com.example.blps.entity.User(
                user.getId(),
                user.getLogin(),
                user.getPassword(),
                user.getName()
        );
    }

    static public com.example.blps.dao.repository.model.User toUserRepoEntity(
            @NonNull com.example.blps.entity.User user
    ) {
        com.example.blps.dao.repository.model.User u1 = new com.example.blps.dao.repository.model.User();
        if (user.getId() != null) u1.setId(user.getId());
        u1.setLogin(user.getLogin());
        u1.setPassword(user.getPassword());
        u1.setName(user.getName());
        return u1;
    }
}
