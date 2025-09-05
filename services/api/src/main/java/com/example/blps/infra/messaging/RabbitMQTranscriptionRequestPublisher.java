package com.example.blps.infra.messaging;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.application.port.out.RequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQTranscriptionRequestPublisher implements RequestPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbit.queue.transcriptionsQueueName:transcriptionsQueue}")
    private String transcriptionsQueueName;

    @Override
    public void publish(Long id) {
        rabbitTemplate.convertAndSend(transcriptionsQueueName, new VideoUploadedEvent(id));
    }
}
