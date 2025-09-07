package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoCleanupService {

    private final VideoInfoRepository videoRepository;
    private final MinioEnlister minioEnlister;

    @Value("${minio.buckets.videos}")
    private String videosBucket;

    @Value("${minio.buckets.transcriptions}")
    private String transcriptionBucket;

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void cleanupOldVideos() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

        List<VideoInfo> oldVideos = videoRepository.findOldVideos(threshold);
        if (oldVideos.isEmpty()) return;

        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

        try {
            for (VideoInfo video : oldVideos) {
                minioXa.removeFile(videosBucket, video.getStorageKey());
                if (!video.getTranscriptionKey().isBlank()) {
                    minioXa.removeFile(transcriptionBucket, video.getTranscriptionKey());
                }
            }

            videoRepository.deleteByIds(
                    oldVideos.stream().map(VideoInfo::getId).toList()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to cleanup videos: " + e.getMessage(), e);
        }
    }
}
