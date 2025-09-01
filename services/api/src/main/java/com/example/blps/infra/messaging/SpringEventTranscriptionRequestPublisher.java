package com.example.blps.infra.messaging;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.application.port.out.RequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventTranscriptionRequestPublisher implements RequestPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Long videoId) {
        publisher.publishEvent(new VideoUploadedEvent(videoId));  // TODO поменять на RabbitMQ. Текущее уже не ловится, удалён event listener
    }
}
