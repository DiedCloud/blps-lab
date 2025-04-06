package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class CommentMapper {
    static public com.example.blps.entity.Comment getComment(
            @NonNull com.example.blps.dao.repository.model.Comment comment
    ) {
        return new com.example.blps.entity.Comment(
                comment.getId(),
                UserMapper.getUser(comment.getAuthor()),
                comment.getComment(),
                comment.getPublished()
        );
    }
}
