package com.example.blps.service;

import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.model.Comment;
import com.example.blps.dao.repository.model.User;
import com.example.blps.dao.repository.model.VideoInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
