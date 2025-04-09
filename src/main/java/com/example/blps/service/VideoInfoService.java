package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.mapper.VideoInfoMapper;
import com.example.blps.entity.User;
import com.example.blps.entity.VideoInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VideoInfoService {
    VideoInfoRepository videoInfoRepository;

    public VideoInfo createVideo(User user, String title, String description) {
        VideoInfo video = new VideoInfo(title, description, LocalDateTime.now(), user);
        videoInfoRepository.save(VideoInfoMapper.toVideoInfoRepoEntity(video));
        return video;
    }
}
