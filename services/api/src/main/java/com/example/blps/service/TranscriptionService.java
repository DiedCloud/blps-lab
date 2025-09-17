package com.example.blps.service;

import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.dao.repository.model.VideoInfo;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TranscriptionService {
    private final VideoService videoService;
    private final MinioClient minioClient;

    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    public ResponseDTOs.ApiResponse<ResponseDTOs.TranscriptionDTO> getTranscriptionByVideoId(Long id) {
        VideoInfo video = videoService.getVideoInfoById(id);
        String transcriptionKey = video.getTranscriptionKey();

        if (transcriptionKey == null || transcriptionKey.isBlank()) {
            return ResponseDTOs.ApiResponse.success(
                    ResponseDTOs.TranscriptionDTO.builder()
                            .transcription("")
                            .build()
            );
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
            return ResponseDTOs.ApiResponse.success(
                    ResponseDTOs.TranscriptionDTO.builder()
                            .transcription(result.toString(StandardCharsets.UTF_8))
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
