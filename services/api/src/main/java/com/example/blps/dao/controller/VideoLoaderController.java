package com.example.blps.dao.controller;

import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.dao.model.VideoDTO;
import com.example.blps.security.UserDetailsImpl;
import com.example.blps.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
@Tag(name = "Video Upload", description = "Operations for uploading videos")
@Slf4j
public class VideoLoaderController {
    private final VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "Video upload service unavailable")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(videoService.uploadVideo(file, title, description, principal.user()));
    }

    @PutMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'edit_any_video')")
    @Operation(summary = "Edit description or title for an existing video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video info edited successfully"),
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> editVideoInfo(
            @PathVariable Long videoId,
            @RequestBody VideoDTO request
    ) {
        return ResponseEntity.ok(videoService.editVideoInfo(videoId, request.getDescription(), request.getTitle()));
    }

    @DeleteMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'delete_any_video')")
    @Operation(summary = "Delete an existing video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video deleted successfully"),
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<?>> deleteVideo(
            @PathVariable Long videoId
    ) {
        videoService.deleteVideoById(videoId);
        return ResponseEntity.ok(ResponseDTOs.ApiResponse.success("Video deleted"));
    }

    @GetMapping("/{videoId}")
    @Operation(summary = "Stream video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "206", description = "Streaming video"),
            @ApiResponse(responseCode = "404", description = "Video not found"),
            @ApiResponse(responseCode = "503", description = "Video service unavailable"),
    })
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long videoId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        ResponseDTOs.StreamVideoResponseDTO video = videoService.streamVideo(videoId, rangeHeader);

        if (rangeHeader == null) {
            return ResponseEntity.ok()
                    .contentLength(video.getContent().length)
                    .contentType(MediaType.valueOf("video/mp4"))
                    .body(new ByteArrayResource(video.getContent()));
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentLength(video.getContent().length)
                    .contentType(MediaType.valueOf("video/mp4"))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes %d-%d/%d".formatted(video.getStart(), video.getEnd(), video.getWholeVideSize()))
                    .body(new ByteArrayResource(video.getContent()));
        }
    }
}