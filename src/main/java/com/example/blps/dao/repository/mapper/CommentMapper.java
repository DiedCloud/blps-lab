package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class CommentMapper {
    static public com.example.blps.entity.Comment getComment(
            @NonNull com.example.blps.dao.repository.model.Comment comment
    ) {
        return new com.example.blps.entity.Comment(
                comment.getId(),
                UserMapper.getUser(comment.getAuthor()),
                comment.getContent(),
                comment.getPublished()
        );
    }

    static public com.example.blps.dao.repository.model.Comment toCommentRepoEntity(
            @NonNull com.example.blps.entity.Comment comment
    ) {
        com.example.blps.dao.repository.model.Comment c1 = new com.example.blps.dao.repository.model.Comment();
        if (comment.getId() != null) c1.setId(comment.getId());
        c1.setAuthor(UserMapper.toUserRepoEntity(comment.getAuthor()));
        c1.setContent(comment.getContent());
        c1.setPublished(comment.getPublished());
        return c1;
    }
}
