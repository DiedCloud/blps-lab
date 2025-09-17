package com.example.blps.service;

import com.example.blps.dao.mapper.ToDTOMapper;
import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.model.Comment;
import com.example.blps.dao.repository.model.ModerationStatus;
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
    private final VideoService videoService;
    private final ProfanityFilter textFilterService;
    private final ModerationService moderationService;

    public ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO> createComment(User author, Long videoId, String text) {
        VideoInfo video = videoService.getVideoInfoById(videoId);

        if (textFilterService.containsBadWords(text)) {
            return ResponseDTOs.ApiResponse.error("Comment contains banned pattern");
        }

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setVideo(video);
        comment.setContent(text);
        comment.setPublished(LocalDateTime.now());
        comment.setStatus(ModerationStatus.PROCESSING);
        commentRepository.save(comment);

        moderationService.moderate(comment);

        return ResponseDTOs.ApiResponse.success(ToDTOMapper.toCommentDTO(comment), "Comment created successfully");
    }

    public ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO> editComment(Long commentId, Long videoId, String text) {
        if (!videoService.checkVideoById(videoId)) throw new NoSuchElementException("Video not found");

        if (textFilterService.containsBadWords(text)) {
            return ResponseDTOs.ApiResponse.error("Comment contains banned pattern");
        }

        var commentRelatedVideo = getCommentById(commentId).getVideo().getId();
        if (!commentRelatedVideo.equals(videoId)) {
            return ResponseDTOs.ApiResponse.error("Video id and comment id do not match");
        }

        Comment comment = getCommentById(commentId);
        comment.setContent(text);
        comment.setStatus(ModerationStatus.PROCESSING);
        commentRepository.save(comment);

        moderationService.moderate(comment);

        return ResponseDTOs.ApiResponse.success(ToDTOMapper.toCommentDTO(comment), "Comment text updated successfully");
    }

    public void dropComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
    }
}
