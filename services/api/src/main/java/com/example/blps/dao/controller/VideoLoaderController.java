package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.ToDTOMapper;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.controller.model.VideoDTO;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.infra.messaging.RabbitMQTranscriptionRequestPublisher;
import com.example.blps.infra.minio.xaresources.MinioEnlister;
import com.example.blps.infra.minio.xaresources.MinioXAResource;
import com.example.blps.exception.VideoLoadingError;
import com.example.blps.security.UserDetailsImpl;
import com.example.blps.service.VideoService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
@Tag(name = "Video Upload", description = "Operations for uploading videos")
@Slf4j
public class VideoLoaderController {
    private final VideoInfoRepository videoRepo;
    private final VideoService videoService;

    private final MinioEnlister minioEnlister;
    private final MinioClient minioClient;
    private final RabbitMQTranscriptionRequestPublisher videoTranscriptionRequestPublisher;

    @Value("${minio.buckets.videos}")
    private String videosBucket;
    @Value("${minio.buckets.transcriptions}")
    private String transcriptionsBucket;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "Video upload service unavailable")
    })
    @Transactional
    public ResponseDTOs.VideoInfoResponseDTO uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @AuthenticationPrincipal UserDetailsImpl principal
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
            video.setAuthor(principal.user());
            videoRepo.save(video);

            String storageKey = "video_%s_%s".formatted(video.getId(), file.getOriginalFilename());
            minioXa.uploadFile(videosBucket, storageKey, file.getInputStream(), file.getSize());

            video.setStorageKey(storageKey);
            videoRepo.save(video);

            videoTranscriptionRequestPublisher.publish(video.getId());

            return ToDTOMapper.toVideoInfoDTO(video);
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to upload video: " + e.getMessage());
        }
    }

    @PutMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'edit_any_video')")
    @Operation(summary = "Edit description or title for an existing video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video info edited successfully"),
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.VideoInfoResponseDTO>> editVideoInfo(
            @PathVariable Long videoId,
            @RequestBody VideoDTO request
    ) {
        VideoInfo video = videoService.getVideoById(videoId);
        video.setDescription(request.getDescription());
        video.setTitle(request.getTitle());
        video.setLastAccessTime(LocalDateTime.now());
        videoService.updateVideo(video);
        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(ToDTOMapper.toVideoInfoDTO(video), "Video info updated successfully")
        );
    }

    @DeleteMapping("/{videoId}")
    @PreAuthorize("hasPermission(#videoId, 'VideoInfo', 'delete_any_video')")
    @Operation(summary = "Delete an existing video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video deleted successfully"),
    })
    @Transactional
    public ResponseEntity<ResponseDTOs.ApiResponse<?>> deleteVideo(
            @PathVariable Long videoId
    ) {
        // Registration of MinioXAResource in current transaction
        MinioXAResource minioXa = minioEnlister.enlistMinioXAResource();

        try {
            VideoInfo video = videoService.getVideoById(videoId);

            minioXa.removeFile(videosBucket, video.getStorageKey());
            if (!video.getTranscriptionKey().isBlank()) {
                minioXa.removeFile(transcriptionsBucket, video.getTranscriptionKey());
            }

            videoRepo.delete(video);

            return ResponseEntity.ok(
                    ResponseDTOs.ApiResponse.success("Video deleted")
            );
        } catch (Exception e) {
            throw new VideoLoadingError("Failed to delete video: " + e.getMessage());
        }
    }

    @GetMapping("/{videoId}")
    @Operation(summary = "Stream video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "206", description = "Streaming video"),
            @ApiResponse(responseCode = "404", description = "Video not found"),
            @ApiResponse(responseCode = "503", description = "Video service unavailable"),
    })
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long videoId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        VideoInfo video = videoService.getVideoById(videoId);
        video.setLastAccessTime(LocalDateTime.now());
        videoService.updateVideo(video);

        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(videosBucket)
                    .object(video.getStorageKey())
                    .build());

            byte[] allBytes = inputStream.readAllBytes();
            long fileSize = allBytes.length;

            if (rangeHeader == null) {
                return ResponseEntity.ok()
                        .contentLength(fileSize)
                        .contentType(MediaType.valueOf("video/mp4"))
                        .body(new ByteArrayResource(allBytes));
            }

            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1 && !ranges[1].isEmpty())
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            if (end >= fileSize) end = fileSize - 1;
            long rangeLength = end - start + 1;

            byte[] partialContent = Arrays.copyOfRange(allBytes, (int) start, (int) end + 1);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes %d-%d/%d".formatted(start, end, fileSize))
                    .body(new ByteArrayResource(partialContent));
        } catch (Exception e) {
            throw new RuntimeException("Failed to stream video", e);
        }
    }
}