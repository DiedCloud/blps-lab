package com.example.blps.infra.messaging;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.application.port.out.RequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class SpringEventTranscriptionRequestPublisher implements RequestPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Long videoId) {
        publisher.publishEvent(new VideoUploadedEvent(videoId));
    }
}
