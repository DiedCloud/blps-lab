package com.example.blps.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventTranscriptionRequestPublisher implements RequestPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Long videoId) {
        publisher.publishEvent(new VideoUploadedEvent(videoId));
    }
}
