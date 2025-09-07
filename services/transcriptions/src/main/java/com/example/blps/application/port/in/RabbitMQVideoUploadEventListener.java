package com.example.blps.application.port.in;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQVideoUploadEventListener {
    private final TranscriptionService transcriptionService;

    @RabbitListener(queues = "${rabbit.queue.transcriptionsQueueName}", messageConverter = "jackson2JsonMessageConverter")
    public void onVideoUploaded(VideoUploadedEvent event) {
        log.info("Received message: {}", event);
        transcriptionService.transcribeVideoById(event.videoId());
    }
}