package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.mapper.VideoInfoMapper;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.entity.User;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoInfoRepository videoRepo;
    private final MinioClient minioClient;
    private final TextFilterService textFilterService;

    public VideoInfo requestMonetization(Long videoId, User user) throws AccessDeniedException {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (!video.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the author can request monetization");

        video.setStatus(MonetizationStatus.PROCESSING);

        String transcription = null;

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("transcriptions")
                            .object(video.getTranscriptionKey())
                            .build()
            );
            transcription = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transcription", e);
        }

        boolean compliant = textFilterService.findBannedWords(transcription);

        if (compliant) {
            video.setStatus(MonetizationStatus.MONETIZED);
        } else {
            video.setStatus(MonetizationStatus.PENDING_MODERATION);
        }

        return videoRepo.save(video);
    }

    public VideoInfo moderate(Long videoId, boolean approved) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (video.getStatus() != MonetizationStatus.PENDING_MODERATION &&
                video.getStatus() != MonetizationStatus.APPEAL_SUBMITTED) {
            throw new IllegalStateException("Video is not under moderation");
        }

        video.setStatus(approved ? MonetizationStatus.MONETIZED : MonetizationStatus.REJECTED);
        return videoRepo.save(video);
    }

    public com.example.blps.entity.VideoInfo getVideoById(Long videoId) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));
        return VideoInfoMapper.getVideoInfo(video);
    }
}

