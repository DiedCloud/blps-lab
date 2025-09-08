package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.service.VideoService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/transcription")
@RequiredArgsConstructor
@Slf4j
public class TranscriptionController {
    private final VideoService videoService;
    private final MinioClient minioClient;

    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @GetMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'edit_any_video')")
    public ResponseEntity<ResponseDTOs.TranscriptionDTO> getTranscription(
            @PathVariable Long videoId
    ) {
        VideoInfo video = videoService.getVideoById(videoId);
        String transcriptionKey = video.getTranscriptionKey();

        if (transcriptionKey == null || transcriptionKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ResponseDTOs.TranscriptionDTO.builder()
                            .transcription("")
                            .build());
        }

        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(transcriptionsBucket)
                .object(transcriptionKey)
                .build());
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            return ResponseEntity.ok(
                    ResponseDTOs.TranscriptionDTO.builder().transcription(result.toString(StandardCharsets.UTF_8)).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}