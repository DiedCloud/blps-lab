package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.DTOMapper;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.User;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.exception.VideoLoadingError;
import com.example.blps.service.TranscriptionService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
@Tag(name = "Video Upload", description = "Operations for uploading videos")
public class VideoLoaderController {
    private final VideoInfoRepository videoRepo;
    private final MinioClient minioClient;
    private final TranscriptionService transcriptionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "Video upload service unavailable")
    })
    public ResponseDTOs.VideoInfoResponseDTO uploadVideo(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("title") String title,
                                                         @RequestParam("description") String description,
                                                         @AuthenticationPrincipal User principal) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        try {
            String storageKey = "video_" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("videos")
                            .object(storageKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            VideoInfo video = new VideoInfo();
            video.setTitle(title);
            video.setDescription(description);
            video.setTranscriptionKey("");
            video.setStorageKey(storageKey);
            video.setPublished(LocalDateTime.now());
            video.setAuthor(principal);

            videoRepo.save(video);

            transcriptionService.transcribeVideo(video);

            return DTOMapper.toVideoInfoDTO(video);
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to upload video: " + e.getMessage());
        }
    }
}