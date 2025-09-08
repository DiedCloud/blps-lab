package com.example.blps.dao.controller.model;

import com.example.blps.dao.repository.model.ModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ResponseDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponseDTO {
        private Long id;
        private String login;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoInfoResponseDTO {
        private String title;
        private String description;
        private String storageKey;
        private String status;
        private String username;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranscriptionDTO {
        private String transcription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResponseDTO {
        private Long id;
        private UserResponseDTO author;
        private String content;
        private LocalDateTime published;
        private ModerationStatus status;
        private Long videoId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppealResponseDTO {
        private Long id;
        private Long videoId;
        private String reason;
        private boolean processed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonetizationInfoResponseDTO {
        private Long videoId;
        private Float percent;
        private Boolean isAgreed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private T data;
        private boolean success;
        private String message;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .data(data)
                    .success(true)
                    .build();
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return ApiResponse.<T>builder()
                    .data(data)
                    .success(true)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse<T> success(String message) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}