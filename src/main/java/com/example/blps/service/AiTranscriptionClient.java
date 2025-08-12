package com.example.blps.service;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.example.blps.exception.VideoLoadingError;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTranscriptionClient {
    private final MinioClient minioClient;
    @Value("${assemblyai.api.key}")
    private String assemblyAiApiKey;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    Transcript getAiTranscription(String storageKey) {
        try {
            InputStream videoStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("videos")
                            .object(storageKey)
                            .build()
            );

            AssemblyAI client = AssemblyAI.builder()
                    .apiKey(assemblyAiApiKey)
                    .build();

            Transcript transcript = client.transcripts().transcribe(videoStream);

            int attempts = 0;
            while (transcript.getStatus() == TranscriptStatus.QUEUED ||
                    transcript.getStatus() == TranscriptStatus.PROCESSING) {
                attempts++;
                if (attempts > 120) {
                    log.error("Transcription timeout after {} attempts", attempts);
                    break;
                }

                Thread.sleep(5000);
                transcript = client.transcripts().get(transcript.getId());
                log.info("Transcription status check {}: {}", attempts, transcript.getStatus());
            }

            return transcript;
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to transcribe video: " + e.getMessage());
        }
    }
}
