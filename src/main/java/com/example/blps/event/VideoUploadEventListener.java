package com.example.blps.event;

import com.example.blps.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VideoUploadEventListener {
    private final TranscriptionService transcriptionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVideoUploaded(VideoUploadedEvent event) {
        // transcribeVideoById помечен @Async, поэтому расчёт на асинхронность
        transcriptionService.transcribeVideoById(event.videoId());
    }
}

