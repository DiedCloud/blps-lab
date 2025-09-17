package com.example.blps.dao.controller;

import com.example.blps.dao.model.NewCommentDTO;
import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.security.UserDetailsImpl;
import com.example.blps.service.CommentService;
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

@RestController
@RequestMapping("video/{videoId}/comment")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Operations for managing comments")
public class CommentController {
    private final CommentService commentService;

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

        ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO> comment =
                commentService.createComment(principal.user(), videoId, request.text());

        if (comment.isSuccess()) {
            return ResponseEntity.ok(comment);
        } else {
            return ResponseEntity.badRequest().body(comment);
        }
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

        ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO> comment =
                commentService.editComment(commentId, videoId, request.text());

        if (comment.isSuccess()) {
            return ResponseEntity.ok(comment);
        } else {
            return ResponseEntity.badRequest().body(comment);
        }
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