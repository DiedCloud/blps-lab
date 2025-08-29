package com.example.blps.service;

import com.example.blps.dao.repository.AppealRepository;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.dao.repository.model.User;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoInfoRepository videoRepo;
    private final AppealRepository appealRepo;
    private final MinioClient minioClient;
    private final TextFilterService textFilterService;

    public VideoInfo requestMonetization(Long videoId, User user) throws AccessDeniedException {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (!video.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the author can request monetization");

        video.setStatus(MonetizationStatus.PROCESSING);

        String transcription;

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("transcriptions")
                            .object(video.getTranscriptionKey())
                            .build()
            );
            transcription = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            video.setStatus(MonetizationStatus.REJECTED);
            videoRepo.save(video);
            throw new NoSuchElementException("Failed to get transcription", e);
        }

        if (textFilterService.containsBannedWord(transcription)) {
            video.setStatus(MonetizationStatus.PENDING_MODERATION);
        } else {
            video.setStatus(MonetizationStatus.MONETIZED);
        }

        return videoRepo.save(video);
    }

    @Transactional
    public VideoInfo moderate(Long videoId, boolean approved) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (video.getStatus() != MonetizationStatus.PENDING_MODERATION &&
                video.getStatus() != MonetizationStatus.APPEAL_SUBMITTED) {
            throw new IllegalStateException("Video is not under moderation");
        }

        // Если была апелляция, то обработаем последнюю
        if (video.getStatus() == MonetizationStatus.APPEAL_SUBMITTED) {
            var appeal = appealRepo.findTopByVideoOrderByIdDesc(video).orElse(null);
            if (appeal != null) {
                appeal.setProcessed(true);
                appealRepo.save(appeal);
            }
        }

        video.setStatus(approved ? MonetizationStatus.MONETIZED : MonetizationStatus.REJECTED);
        return videoRepo.save(video);
    }

    public VideoInfo getVideoById(Long videoId) {
        return videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));
    }

    public boolean checkVideoById(Long videoId) {
        return videoRepo.existsById(videoId);
    }
}

