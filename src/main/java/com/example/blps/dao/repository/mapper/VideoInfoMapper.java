package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class VideoInfoMapper {
    static public com.example.blps.entity.VideoInfo getVideoInfo(
            @NonNull com.example.blps.dao.repository.model.VideoInfo videoInfo
    ) {
        return new com.example.blps.entity.VideoInfo(
                videoInfo.getId(),
                videoInfo.getTitle(),
                videoInfo.getDescription(),
                videoInfo.getPublished(),
                UserMapper.getUser(videoInfo.getAuthor())
        );
    }
}
