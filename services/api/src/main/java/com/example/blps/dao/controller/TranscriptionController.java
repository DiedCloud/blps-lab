package com.example.blps.dao.controller;

import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transcription")
@RequiredArgsConstructor
@Slf4j
public class TranscriptionController {
    private final TranscriptionService transcriptionService;

    @GetMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'edit_any_video')")
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.TranscriptionDTO>> getTranscription(
            @PathVariable Long videoId
    ) {

        var videoInfo = transcriptionService.getTranscriptionByVideoId(videoId);

        return ResponseEntity.ok(videoInfo);
    }
}