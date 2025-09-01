package com.example.blps.service;

import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.model.Comment;
import com.example.blps.dao.repository.model.User;
import com.example.blps.dao.repository.model.VideoInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment createComment(User author, VideoInfo video, String text) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setVideo(video);
        comment.setContent(text);
        comment.setPublished(LocalDateTime.now());
        commentRepository.save(comment);
        return comment;
    }

    public Comment editComment(Long commentId, String text) {
        Comment comment = getCommentById(commentId);
        comment.setContent(text);
        commentRepository.save(comment);
        return comment;
    }

    public void dropComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
    }
}
