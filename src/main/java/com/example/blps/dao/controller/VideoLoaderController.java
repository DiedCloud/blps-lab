package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.DTOMapper;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.User;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.dao.xaresources.MinioEnlister;
import com.example.blps.dao.xaresources.MinioXAResource;
import com.example.blps.dao.xaresources.MinioXATransactionalResource;
import com.example.blps.exception.VideoLoadingError;
import com.example.blps.service.TranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
@Tag(name = "Video Upload", description = "Operations for uploading videos")
@Slf4j
public class VideoLoaderController {
    private final VideoInfoRepository videoRepo;
    private final TranscriptionService transcriptionService;

    private final MinioEnlister minioEnlister;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "Video upload service unavailable")
    })
    @Transactional
    public ResponseDTOs.VideoInfoResponseDTO uploadVideo(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("title") String title,
                                                         @RequestParam("description") String description,
                                                         @AuthenticationPrincipal User principal) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Registration of MinioXAResource in current transaction
        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

        try {
            VideoInfo video = new VideoInfo();
            video.setTitle(title);
            video.setDescription(description);
            video.setTranscriptionKey("");
            video.setStorageKey("");
            video.setPublished(LocalDateTime.now());
            video.setAuthor(principal);
            videoRepo.save(video);

            String storageKey = "video_" + video.getId() + "_user_" + principal.getLogin() + "_" + file.getOriginalFilename();
            minioXa.uploadFile("videos", storageKey, file.getInputStream(), file.getSize());

            video.setStorageKey(storageKey);
            videoRepo.save(video);

            // transcriptionService.transcribeVideo(video);

            return DTOMapper.toVideoInfoDTO(video);
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to upload video: " + e.getMessage());
        }
    }
}