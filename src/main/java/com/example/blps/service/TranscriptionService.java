package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import com.example.blps.infra.transcription.ProfanityFilter;
import com.example.blps.infra.transcription.WhisperTranscriptionUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {
    private final MinioClient minioClient;
    private final VideoInfoRepository videoRepo;
    private final WhisperTranscriptionUtils whisper;
    private final ProfanityFilter filter;

    private final MinioEnlister minioEnlister;

    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @Async("transcribeExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> transcribeVideoById(Long videoId) {

        // Registration of MinioXAResource in current transaction
        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Video not found: " + videoId));

        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("videos")
                        .object(video.getStorageKey())
                        .build()
        )) {
            String transcription = whisper.getTranscription(is);

            if (filter.containsBadWords(transcription)) {
                throw new IllegalStateException("Video contains forbidden materials");
            }

            String transcriptionKey = saveTranscription(
                    video.getId(),
                    transcription,
                    minioXa
            );
            video.setTranscriptionKey(transcriptionKey);
            video.setStatus(MonetizationStatus.MONETIZED);
            videoRepo.save(video);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Transcription failed for videoId={}", videoId, e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
            VideoInfo entity = videoRepo.findById(videoId).orElseThrow();
            entity.setStatus(MonetizationStatus.REJECTED);
            videoRepo.save(entity);
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)  // mandatory, так как работает через XA ресурс
    String saveTranscription(Long videoId, String transcription, MinioXAResource minioXa) throws Exception {
        String storageKey = "video_" + videoId + "_transcription.txt";
        byte[] bytes = transcription.getBytes(StandardCharsets.UTF_8);
        minioXa.uploadFile(
                transcriptionsBucket,
                storageKey,
                new ByteArrayInputStream(bytes),
                bytes.length
        );
        return storageKey;
    }

    public String getTranscription(String key) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("transcriptions")
                            .object(key)
                            .build()
            );
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transcription", e);
        }
    }
}
