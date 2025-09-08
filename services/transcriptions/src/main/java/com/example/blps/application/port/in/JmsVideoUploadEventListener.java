package com.example.blps.application.port.in;

import com.example.blps.application.event.VideoUploadedEvent;
import com.example.blps.service.TranscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JmsVideoUploadEventListener {
    private final TranscriptionService transcriptionService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${rabbit.queue.transcriptionsQueueName}", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(Message message) throws JMSException, JsonProcessingException {
        if (message instanceof TextMessage textMessage) {
            String json = textMessage.getText();
            VideoUploadedEvent event = objectMapper.readValue(json, VideoUploadedEvent.class);
            log.info("Received JMS message (manual): {}", event);
            transcriptionService.transcribeVideoById(event.videoId());
        } else {
            log.warn("Unsupported JMS message type: {}", message.getClass());
        }
    }
}