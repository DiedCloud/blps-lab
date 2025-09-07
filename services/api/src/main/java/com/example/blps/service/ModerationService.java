package com.example.blps.service;

import com.example.blps.dao.controller.model.ModerationResultDTO;
import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.model.Comment;
import com.example.blps.dao.repository.model.ModerationStatus;
import com.example.blps.jca.HiveConnection;
import com.example.blps.jca.HiveConnectionFactory;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModerationService {
    private final HiveConnectionFactory hiveConnectionFactory;
    private final CommentRepository commentRepository;

    @Async
    public void moderate(Comment comment) {
        try (HiveConnection conn = (HiveConnection) hiveConnectionFactory.getConnection()) {
            ModerationResultDTO res = conn.moderate(comment.getContent());

            if (res.approved()) {
                comment.setStatus(ModerationStatus.APPROVED);
            } else {
                comment.setStatus(ModerationStatus.REJECTED);
            }

            commentRepository.save(comment);
        } catch (ResourceException e) {
            throw new IllegalStateException("Hive moderation failed", e);
        }
    }
}
