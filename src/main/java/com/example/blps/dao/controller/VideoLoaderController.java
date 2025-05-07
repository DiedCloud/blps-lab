package com.example.blps.dao.controller;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.mapper.VideoInfoMapper;
import com.example.blps.entity.User;
import com.example.blps.entity.VideoInfo;
import com.example.blps.exception.VideoLoadingError;
import com.example.blps.service.TranscriptionService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoLoaderController {
    private final VideoInfoRepository videoRepo;
    private final MinioClient minioClient;
    private final TranscriptionService transcriptionService;

    @PostMapping("/upload")
    public VideoInfo uploadVideo(@RequestParam("file") MultipartFile file,
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

            VideoInfo video = new VideoInfo(title, description, "", storageKey, LocalDateTime.now(), principal);

            videoRepo.save(VideoInfoMapper.toVideoInfoRepoEntity(video));

            transcriptionService.transcribeVideo(video);

            return video;
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to upload video: " + e.getMessage());
        }
    }
}