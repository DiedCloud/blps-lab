package com.example.blps.service;

import com.example.blps.dao.repository.AppealRepository;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.Appeal;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.dao.repository.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AppealService {
    private final AppealRepository appealRepo;
    private final VideoInfoRepository videoRepo;

    public Appeal submitAppeal(Long videoId, String reason, User user) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (!video.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the author can appeal");

        if (appealRepo.existsByVideo(video))
            throw new IllegalStateException("Appeal already submitted");

        video.setStatus(MonetizationStatus.APPEAL_SUBMITTED);
        videoRepo.save(video);

        Appeal appeal = new Appeal();
        appeal.setVideo(video);
        appeal.setReason(reason);
        appeal.setProcessed(false);

        return appealRepo.save(appeal);
    }
}

