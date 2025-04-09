package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.blps.entity.User;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoInfoRepository videoRepo;

    public VideoInfo requestMonetization(Long videoId, User user) throws AccessDeniedException {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (!video.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the author can request monetization");

        video.setStatus(MonetizationStatus.PROCESSING);

        boolean compliant = !video.getDescription().toLowerCase().contains("violence");

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
}

