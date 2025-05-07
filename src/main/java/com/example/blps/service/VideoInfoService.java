package com.example.blps.service;

import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.mapper.VideoInfoMapper;
import com.example.blps.entity.User;
import com.example.blps.entity.VideoInfo;
import com.example.blps.exception.VideoLoadingError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class VideoInfoService {
    VideoInfoRepository videoInfoRepository;

    public VideoInfo createVideo(User user, String title, String description) throws VideoLoadingError {

        // Ultra genius mock:
        if (new Random().nextInt(1, 100) <= 15) { // 15% шанс :)
            throw new VideoLoadingError("Video storage unavailable");
        }

        VideoInfo video = new VideoInfo(title, description, "", "", LocalDateTime.now(), user);

        return VideoInfoMapper.getVideoInfo(
                videoInfoRepository.save(VideoInfoMapper.toVideoInfoRepoEntity(video))
        );
    }
}
