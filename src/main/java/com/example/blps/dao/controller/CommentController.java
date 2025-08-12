package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.DTOMapper;
import com.example.blps.dao.controller.model.NewCommentDTO;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.model.User;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.service.CommentService;
import com.example.blps.service.TextFilterService;
import com.example.blps.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Operations for managing comments")
public class CommentController {
    private final CommentService commentService;
    private final TextFilterService textFilterService;
    private final VideoService videoService;

    @PostMapping("/new")
    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Comment contains banned pattern")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.CommentResponseDTO>> createComment(
            @Valid @RequestBody NewCommentDTO request,
            @AuthenticationPrincipal User principal) {
        VideoInfo video = videoService.getVideoById(request.getVideoId());

        if (textFilterService.containsBannedWord(request.getText())) {
            return ResponseEntity.badRequest().body(
                    ResponseDTOs.ApiResponse.error("Comment contains banned pattern")
            );
        }

        var comment = commentService.createComment(principal, video, request.getText());
        var commentDTO = DTOMapper.toCommentDTO(comment);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(commentDTO, "Comment created successfully")
        );
    }
}