package com.example.blps.service;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.exception.VideoLoadingError;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {
    private final MinioClient minioClient;
    private final VideoInfoRepository videoRepo;

    @Value("${assemblyai.api.key}")
    private String assemblyAiApiKey;

    @Value("${assemblyai.mock_requests}")
    private Boolean needToMockRequest;

    @Async
    public void transcribeVideo(VideoInfo video) {
        try {
            if (needToMockRequest) {
                String transcriptionText = "Mocked video transcription";
                String transcriptionKey = saveTranscription(transcriptionText);

                video.setTranscriptionKey(transcriptionKey);
                videoRepo.save(video);
                return;
            }

            InputStream videoStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("videos")
                            .object(video.getStorageKey())
                            .build()
            );

            File tempFile = File.createTempFile("video_", ".mp4");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                videoStream.transferTo(fos);
            }
            log.info("Video downloaded to temp file: {}", tempFile.getAbsolutePath());

            AssemblyAI client = AssemblyAI.builder()
                    .apiKey(assemblyAiApiKey)
                    .build();

            Transcript transcript = client.transcripts().transcribe(tempFile);

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

            tempFile.delete();

            if (transcript.getStatus() == TranscriptStatus.COMPLETED) {
                String transcriptionText = transcript.getText().get();
                String transcriptionKey = saveTranscription(transcriptionText);

                video.setTranscriptionKey(transcriptionKey);
                videoRepo.save(video);
            } else {
                log.error("Transcription failed with status: {}", transcript.getStatus());
            }

        } catch (Exception e) {
            throw new VideoLoadingError("Failed to load video: " + e.getMessage());
        }
    }

    private String saveTranscription(String transcription) throws Exception {
        String transcriptionKey = "transcript_" + System.currentTimeMillis() + ".txt";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("transcriptions")
                        .object(transcriptionKey)
                        .stream(new ByteArrayInputStream(transcription.getBytes(StandardCharsets.UTF_8)),
                                transcription.length(), -1)
                        .contentType("text/plain")
                        .build()
        );

        return transcriptionKey;
    }

    public String getTranscription(String key) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("transcriptions")
                            .object(key)
                            .build()
            );
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transcription", e);
        }
    }
}
