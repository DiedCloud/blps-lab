package com.example.blps.service;

import com.example.blps.dao.mapper.ToDTOMapper;
import com.example.blps.dao.model.ResponseDTOs;
import com.example.blps.dao.repository.AppealRepository;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.MonetizationStatus;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.dao.repository.model.User;
import com.example.blps.exception.VideoLoadingError;
import com.example.blps.infra.messaging.RabbitMQTranscriptionRequestPublisher;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoInfoRepository videoRepo;
    private final AppealRepository appealRepo;

    private final MinioEnlister minioEnlister;
    private final MinioClient minioClient;

    private final ProfanityFilter textFilterService;

    @Value("${minio.buckets.videos}")
    private String videosBucket;
    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    private final RabbitMQTranscriptionRequestPublisher videoTranscriptionRequestPublisher;


    public ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO> requestMonetization(Long videoId, User user) throws AccessDeniedException {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (!video.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the author can request monetization");

        video.setStatus(MonetizationStatus.PROCESSING);

        String transcription;

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("transcriptions")
                            .object(video.getTranscriptionKey())
                            .build()
            );
            transcription = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            video.setStatus(MonetizationStatus.REJECTED);
            videoRepo.save(video);
            throw new NoSuchElementException("Failed to get transcription", e);
        }

        if (textFilterService.containsBadWords(transcription)) {
            video.setStatus(MonetizationStatus.PENDING_MODERATION);
        } else {
            video.setStatus(MonetizationStatus.MONETIZED);
        }

        videoRepo.save(video);

        return ResponseDTOs.ApiResponse.success(ToDTOMapper.toVideoInfoDTO(video), "Monetization request processed successfully");
    }

    @Transactional
    public ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO> moderate(Long videoId, boolean approved) {
        VideoInfo video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        if (video.getStatus() != MonetizationStatus.PENDING_MODERATION &&
                video.getStatus() != MonetizationStatus.APPEAL_SUBMITTED) {
            throw new IllegalStateException("Video is not under moderation");
        }

        // Если была апелляция, то обработаем последнюю
        if (video.getStatus() == MonetizationStatus.APPEAL_SUBMITTED) {
            var appeal = appealRepo.findTopByVideoOrderByIdDesc(video).orElse(null);
            if (appeal != null) {
                appeal.setProcessed(true);
                appealRepo.save(appeal);
            }
        }

        video.setStatus(approved ? MonetizationStatus.MONETIZED : MonetizationStatus.REJECTED);
        videoRepo.save(video);

        return ResponseDTOs.ApiResponse.success(ToDTOMapper.toVideoInfoDTO(video), "Moderation completed successfully");
    }

    public VideoInfo getVideoInfoById(Long videoId) {
        return videoRepo.findById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Video not found"));
    }

    public boolean checkVideoById(Long videoId) {
        return videoRepo.existsById(videoId);
    }

    public VideoInfo updateVideoAccessTime(Long videoId) {
        return updateVideoAccessTime(videoId, LocalDateTime.now());
    }

    public VideoInfo updateVideoAccessTime(Long videoId, LocalDateTime accessTime) {
        var video = getVideoInfoById(videoId);
        video.setLastAccessTime(accessTime);
        videoRepo.save(video);
        return video;
    }

    public ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO> editVideoInfo(Long videoId, String description, String title) {
        VideoInfo video = getVideoInfoById(videoId);

        video.setDescription(description);
        video.setTitle(title);
        video.setLastAccessTime(LocalDateTime.now());
        videoRepo.save(video);

        return ResponseDTOs.ApiResponse.success(ToDTOMapper.toVideoInfoDTO(video), "Video info updated successfully");
    }

    @Transactional
    public void deleteVideoById(Long videoId) {
        // Registration of MinioXAResource in current transaction
        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

        try {
            VideoInfo video = getVideoInfoById(videoId);

            minioXa.removeFile(videosBucket, video.getStorageKey());
            if (!video.getTranscriptionKey().isBlank()) {
                minioXa.removeFile(transcriptionsBucket, video.getTranscriptionKey());
            }

            videoRepo.delete(video);

        } catch (Exception e) {
            throw new VideoLoadingError("Failed to delete video: " + e.getMessage());
        }
    }

    public ResponseDTOs.StreamVideoResponseDTO streamVideo(Long videoId, String rangeHeader) {
        VideoInfo video = updateVideoAccessTime(videoId);

        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(videosBucket)
                    .object(video.getStorageKey())
                    .build());

            byte[] allBytes = inputStream.readAllBytes();
            long fileSize = allBytes.length;

            if (rangeHeader == null) {
                return ResponseDTOs.StreamVideoResponseDTO.builder().content(allBytes).build();
            }

            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty())
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            if (end >= fileSize) end = fileSize - 1;

            return ResponseDTOs.StreamVideoResponseDTO.builder()
                    .content(Arrays.copyOfRange(allBytes, (int) start, (int) end + 1))
                    .start(start)
                    .end(end)
                    .wholeVideSize(fileSize)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream video", e);
        }
    }

    @Transactional
    public ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO> uploadVideo(
            MultipartFile file, String description, String title, User user
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Registration of MinioXAResource in current transaction
        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();
        LocalDateTime published = LocalDateTime.now();

        try {
            VideoInfo video = new VideoInfo();
            video.setTitle(title);
            video.setDescription(description);
            video.setTranscriptionKey("");
            video.setStorageKey("");
            video.setPublished(published);
            video.setLastAccessTime(published);
            video.setAuthor(user);
            videoRepo.save(video);

            String storageKey = "video_%s_%s".formatted(video.getId(), file.getOriginalFilename());
            minioXa.uploadFile(videosBucket, storageKey, file.getInputStream(), file.getSize());

            video.setStorageKey(storageKey);
            videoRepo.save(video);

            videoTranscriptionRequestPublisher.publish(video.getId());

            return ResponseDTOs.ApiResponse.success(ToDTOMapper.toVideoInfoDTO(video));
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to upload video: " + e.getMessage());
        }
    }
}

