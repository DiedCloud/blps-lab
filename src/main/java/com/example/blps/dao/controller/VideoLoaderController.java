package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.VideoDTO;
import com.example.blps.entity.VideoInfo;
import com.example.blps.service.UserService;
import com.example.blps.service.VideoInfoService;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
@AllArgsConstructor
public class VideoLoaderController {
    VideoInfoService videoService;
    UserService userService;
    MinioClient minioClient;

    @CrossOrigin
    @PostMapping("/new")
    public ResponseEntity<Long> login(@RequestBody final VideoDTO request) {
        VideoInfo res = videoService.createVideo(
                userService.getCurrentUser(),
                request.getTitle(),
                request.getDescription()
        );
        return ResponseEntity.ok(res.getId());
    }
}