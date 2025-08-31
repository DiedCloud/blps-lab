package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TranscriptionXaService {
    private final VideoInfoRepository videoRepo;
    private final MinioEnlister minioEnlister;
    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTranscriptionAndStatus(Long videoId, String transcription, MonetizationStatus status) throws Exception {
        VideoInfo video = videoRepo.findById(videoId).orElseThrow();

        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();
        String storageKey = "video_" + videoId + "_transcription.txt";

        minioXa.uploadFile(transcriptionsBucket, storageKey,
                new ByteArrayInputStream(transcription.getBytes(StandardCharsets.UTF_8)),
                transcription.getBytes(StandardCharsets.UTF_8).length);

        video.setTranscriptionKey(storageKey);
        video.setStatus(status);
        videoRepo.save(video);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateVideoStatus(Long videoId, MonetizationStatus status) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow();
        video.setStatus(status);
        videoRepo.save(video);
    }
}
