package com.example.blps.service;

import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.UserRepository;
import com.example.blps.dao.repository.mapper.CommentMapper;
import com.example.blps.entity.Comment;
import com.example.blps.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private static final List<String> BANNED_WORDS = List.of("badword", "anotherbadword");
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public boolean validateComment(String text) {
        for (String banned : BANNED_WORDS) {
            if (text.toLowerCase().contains(banned)) {
                return false;
            }
        }
        return true;
    }

    public Comment createComment(User author, String text) {
        Comment comment = new Comment(author, text, LocalDateTime.now());
        return CommentMapper.getComment(
                commentRepository.save(CommentMapper.toCommentRepoEntity(comment))
        );
    }
}
