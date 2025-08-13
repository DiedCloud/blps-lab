package com.example.blps.service;

import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.assemblyai.AiTranscriptionClient;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {
    private final MinioClient minioClient;
    private final VideoInfoRepository videoRepo;
    private final AiTranscriptionClient aiTranscriptionClient;

    private final MinioEnlister minioEnlister;

    @Value("${assemblyai.mock_requests}")
    private Boolean needToMockRequest;
    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @Async("transcribeExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> transcribeVideoById(Long videoId) {
        try {
            // Registration of MinioXAResource in current transaction
            MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

            VideoInfo video = videoRepo.findById(videoId)
                    .orElseThrow(() -> new IllegalStateException("Video not found: " + videoId));

            // Идемпотентность: если уже есть ключ — пропускаем todo а надо ли в целом?
            if (video.getTranscriptionKey() != null && !video.getTranscriptionKey().isBlank()) {
                log.info("Video {} already has transcription key {}, skipping", videoId, video.getTranscriptionKey());
                return CompletableFuture.completedFuture(null);
            }

            if (needToMockRequest) {
                String transcriptionKey = saveTranscription(
                        video.getId(),
                        "Mocked video transcription",
                        minioXa
                );
                video.setTranscriptionKey(transcriptionKey);
                videoRepo.save(video);
                return CompletableFuture.completedFuture(null);
            }

            Transcript transcript = aiTranscriptionClient.getAiTranscription(video.getStorageKey());

            if (transcript.getStatus() != TranscriptStatus.COMPLETED) {
                log.error("Transcription not completed for video {}: {}", videoId, transcript.getStatus());
                return CompletableFuture.completedFuture(null);
            }

            String transcriptionKey = saveTranscription(
                    video.getId(),
                    transcript.getText().orElse("No transcription found"),
                    minioXa
            );
            video.setTranscriptionKey(transcriptionKey);
            videoRepo.save(video);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Transcription failed for videoId={}", videoId, e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
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
