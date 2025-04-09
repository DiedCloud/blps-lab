package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.ModerationRequestDTO;
import com.example.blps.dao.controller.model.MonetizationRequestDTO;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.entity.User;
import com.example.blps.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/monetize")
    public VideoInfo monetize(@RequestBody MonetizationRequestDTO req,
                              @AuthenticationPrincipal User principal) throws AccessDeniedException {
        return videoService.requestMonetization(req.videoId(), principal);
    }

    @PostMapping("/moderate")
    public VideoInfo moderate(@RequestBody ModerationRequestDTO req) {
        return videoService.moderate(req.videoId(), req.approved());
    }
}

