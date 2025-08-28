package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.transcription.ProfanityFilter;
import com.example.blps.infra.transcription.WhisperTranscriptionUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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
    private final TranscriptionXaService transcriptionXaService;

    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @Async("transcribeExecutor")
    public CompletableFuture<Void> transcribeVideoById(Long videoId) {
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
                transcriptionXaService.updateVideoStatus(video.getId(), MonetizationStatus.REJECTED);
                return CompletableFuture.failedFuture(
                        new IllegalStateException("Video contains forbidden materials"));
            }

            transcriptionXaService.saveTranscriptionAndStatus(video.getId(), transcription, MonetizationStatus.REJECTED);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Transcription failed for videoId={}", videoId, e);
            transcriptionXaService.updateVideoStatus(videoId, MonetizationStatus.REJECTED);
            return CompletableFuture.failedFuture(e);
        }
    }

    public String getTranscription(String key) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(transcriptionsBucket)
                        .object(key)
                        .build()
        )) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transcription", e);
        }
    }
}
