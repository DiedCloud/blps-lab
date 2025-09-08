package com.example.blps.infra.messaging;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.application.port.out.RequestPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQTranscriptionRequestPublisher implements RequestPublisher {
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbit.queue.transcriptionsQueueName:transcriptionsQueue}")
    private String transcriptionsQueueName;

    @Override
    public void publish(Long id) {
        try {
            String json = objectMapper.writeValueAsString(new VideoUploadedEvent(id));
            jmsTemplate.send(transcriptionsQueueName, session -> {
                TextMessage message = session.createTextMessage(json);
                return message;
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize VideoUploadedEvent", e);
        }
    }
}
