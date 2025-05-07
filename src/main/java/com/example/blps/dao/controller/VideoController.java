package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.ModerationRequestDTO;
import com.example.blps.dao.controller.model.MonetizationRequestDTO;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dto.mapper.DTOMapper;
import com.example.blps.entity.User;
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

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video Management", description = "Operations for video management")
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/monetize")
    @Operation(summary = "Request monetization for a video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monetization request successful"),
            @ApiResponse(responseCode = "403", description = "Access denied - not the author"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> monetize(
            @Valid @RequestBody MonetizationRequestDTO req,
            @AuthenticationPrincipal User principal) throws AccessDeniedException {

        var videoInfo = videoService.requestMonetization(req.videoId(), principal);
        var responseDTO = DTOMapper.toVideoInfoDTO(videoInfo);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(responseDTO, "Monetization request processed successfully")
        );
    }

    @PostMapping("/moderate")
    @Operation(summary = "Moderate a video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Moderation successful"),
            @ApiResponse(responseCode = "400", description = "Video is not under moderation"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> moderate(
            @Valid @RequestBody ModerationRequestDTO req) {

        var videoInfo = videoService.moderate(req.videoId(), req.approved());
        var responseDTO = DTOMapper.toVideoInfoDTO(videoInfo);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(responseDTO, "Moderation completed successfully")
        );
    }
}