package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.ToDTOMapper;
import com.example.blps.dao.controller.model.AppealRequestDTO;
import com.example.blps.dao.controller.model.ModerationRequestDTO;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.model.User;
import com.example.blps.service.AppealService;
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

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video Management", description = "Operations for managing video monetization")
public class MonetizationController {
    private final VideoService videoService;
    private final AppealService appealService;

    @PostMapping("/monetize/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'request_monetization_on_any_video')")
    @Operation(summary = "Request monetization for a video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monetization request successful"),
            @ApiResponse(responseCode = "403", description = "Access denied - not the author"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> monetize(
            @PathVariable Long videoId,
            @AuthenticationPrincipal User principal
    ) throws AccessDeniedException {

        var videoInfo = videoService.requestMonetization(videoId, principal);
        var responseDTO = ToDTOMapper.toVideoInfoDTO(videoInfo);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(responseDTO, "Monetization request processed successfully")
        );
    }

    @PostMapping("/moderate/{videoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN', 'moderate_monetization_request')")
    @Operation(summary = "Moderate a video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Moderation successful"),
            @ApiResponse(responseCode = "400", description = "Video is not under moderation"),
            @ApiResponse(responseCode = "403", description = "Access denied - not enough privileges"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> moderate(
            @PathVariable Long videoId,
            @Valid @RequestBody ModerationRequestDTO req
    ) {

        var videoInfo = videoService.moderate(videoId, req.approved());
        var responseDTO = ToDTOMapper.toVideoInfoDTO(videoInfo);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(responseDTO, "Moderation completed successfully")
        );
    }

    @PostMapping("appeal/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'appeal_monetization_on_any_video')")
    @Operation(summary = "Submit an appeal for a rejected video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appeal submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Appeal already submitted"),
            @ApiResponse(responseCode = "403", description = "Access denied - not the author"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AppealResponseDTO>> submitAppeal(
            @PathVariable Long videoId,
            @Valid @RequestBody AppealRequestDTO req,
            @AuthenticationPrincipal User principal
    ) {

        var appeal = appealService.submitAppeal(videoId, req.reason(), principal);
        ResponseDTOs.AppealResponseDTO appealDTO = ToDTOMapper.toAppealDTO(appeal);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(appealDTO, "Appeal submitted successfully")
        );
    }
}