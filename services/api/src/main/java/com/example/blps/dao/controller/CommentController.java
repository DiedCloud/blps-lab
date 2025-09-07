package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.ToDTOMapper;
import com.example.blps.dao.controller.model.NewCommentDTO;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.model.ModerationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.security.UserDetailsImpl;
import com.example.blps.service.ModerationService;
import com.example.blps.service.ProfanityFilter;
import com.example.blps.service.CommentService;
import com.example.blps.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("video/{videoId}/comment")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Operations for managing comments")
public class CommentController {
    private final CommentService commentService;
    private final ProfanityFilter textFilterService;
    private final VideoService videoService;
    private final ModerationService moderationService;

    @PostMapping
    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Comment contains banned pattern")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO>> createComment(
            @PathVariable Long videoId,
            @Valid @RequestBody NewCommentDTO request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {

        VideoInfo video = videoService.getVideoById(videoId);

        if (textFilterService.containsBadWords(request.text())) {
            return ResponseEntity.badRequest().body(
                    ResponseDTOs.ApiResponse.error("Comment contains banned pattern")
            );
        }

        var comment = commentService.createComment(principal.user(), video, request.text());
        moderationService.moderate(comment);
        var commentDTO = ToDTOMapper.toCommentDTO(comment);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(commentDTO, "Comment created successfully")
        );
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasPermission(#commentId, 'Comment', 'edit_any_comment')")
    @Operation(summary = "Edit an existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment edited successfully"),
            @ApiResponse(responseCode = "400", description = "Comment contains banned pattern")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO>> editComment(
            @PathVariable Long commentId,
            @PathVariable Long videoId,
            @Valid @RequestBody NewCommentDTO request
    ) {

        if (!videoService.checkVideoById(videoId)) throw new NoSuchElementException("Video not found");

        if (textFilterService.containsBadWords(request.text())) {
            return ResponseEntity.badRequest().body(
                    ResponseDTOs.ApiResponse.error("Comment contains banned pattern")
            );
        }

        var commentRelatedVideo = commentService.getCommentById(commentId).getVideo().getId();
        if (!commentRelatedVideo.equals(videoId)) {
            return ResponseEntity.badRequest().body(
                    ResponseDTOs.ApiResponse.error("Video id and comment id do not match")
            );
        }

        var comment = commentService.editComment(commentId, request.text());
        moderationService.moderate(comment);
        var commentDTO = ToDTOMapper.toCommentDTO(comment);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(commentDTO, "Comment text updated successfully")
        );
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasPermission(#commentId, 'Comment', 'delete_any_comment') || hasPermission(#videoId, 'VideoInfo', 'delete_any_comment')")
    @Operation(summary = "Delete an existing comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<?>> deleteComment(
            @PathVariable Long commentId,
            @PathVariable Long videoId
    ) {
        commentService.dropComment(commentId);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success("Comment deleted")
        );
    }
}