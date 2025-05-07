package com.example.blps.dao.repository.mapper;

import com.example.blps.dao.repository.model.MonetizationStatus;
import lombok.NonNull;

public class VideoInfoMapper {
    static public com.example.blps.entity.VideoInfo getVideoInfo(
            @NonNull com.example.blps.dao.repository.model.VideoInfo videoInfo
    ) {
        return new com.example.blps.entity.VideoInfo(
                videoInfo.getId(),
                videoInfo.getTitle(),
                videoInfo.getDescription(),
                videoInfo.getTranscriptionKey(),
                videoInfo.getStorageKey(),
                videoInfo.getPublished(),
                UserMapper.getUser(videoInfo.getAuthor())
        );
    }

    static public com.example.blps.dao.repository.model.VideoInfo toVideoInfoRepoEntity(
            @NonNull com.example.blps.entity.VideoInfo video
    ) {
        com.example.blps.dao.repository.model.VideoInfo v1 = new com.example.blps.dao.repository.model.VideoInfo();
        if (video.getId() != null) v1.setId(video.getId());
        v1.setTitle(video.getTitle());
        v1.setDescription(video.getDescription());
        v1.setStorageKey(video.getStorageKey());
        v1.setStatus(MonetizationStatus.PROCESSING);
        v1.setTranscriptionKey(video.getTranscriptionKey());
        v1.setPublished(video.getPublished());
        v1.setAuthor(UserMapper.toUserRepoEntity(video.getAuthor()));
        return v1;
    }
}
