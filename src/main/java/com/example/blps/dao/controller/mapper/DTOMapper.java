package com.example.blps.dao.controller.mapper;

import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.dao.repository.model.Appeal;
import com.example.blps.dao.repository.model.Comment;
import com.example.blps.dao.repository.model.MonetizationInfo;
import com.example.blps.dao.repository.model.VideoInfo;
import com.example.blps.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class DTOMapper {

    public static ResponseDTOs.UserResponseDTO toUserDTO(User user) {
        if (user == null) return null;

        return ResponseDTOs.UserResponseDTO.builder()
                .id(user.getId())
                .login(user.getLogin())
                .name(user.getName())
                .build();
    }

    public static ResponseDTOs.UserResponseDTO toUserDTO(com.example.blps.dao.repository.model.User user) {
        if (user == null) return null;

        return ResponseDTOs.UserResponseDTO.builder()
                .id(user.getId())
                .login(user.getLogin())
                .name(user.getName())
                .build();
    }

    public static ResponseDTOs.VideoInfoResponseDTO toVideoInfoDTO(VideoInfo video) {
        if (video == null) return null;

        return ResponseDTOs.VideoInfoResponseDTO.builder()
                .title(video.getTitle())
                .description(video.getDescription())
                .storageKey(video.getStorageKey())
                .username(video.getAuthor().getLogin())
                .status(video.getStatus() != null ? video.getStatus().name() : null)
                .build();
    }

    public static ResponseDTOs.VideoInfoResponseDTO toVideoInfoDTO(com.example.blps.entity.VideoInfo video) {
        if (video == null) return null;

        return ResponseDTOs.VideoInfoResponseDTO.builder()
                .title(video.getTitle())
                .description(video.getDescription())
                .storageKey(video.getStorageKey())
                .build();
    }

    public static ResponseDTOs.CommentResponseDTO toCommentDTO(Comment comment) {
        if (comment == null) return null;

        return ResponseDTOs.CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .published(comment.getPublished())
                .author(toUserDTO(comment.getAuthor()))
                .build();
    }

    public static ResponseDTOs.CommentResponseDTO toCommentDTO(com.example.blps.entity.Comment comment) {
        if (comment == null) return null;

        return ResponseDTOs.CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .published(comment.getPublished())
                .author(toUserDTO(comment.getAuthor()))
                .videoId(comment.getVideo().getId())
                .build();
    }

    public static ResponseDTOs.AppealResponseDTO toAppealDTO(Appeal appeal) {
        if (appeal == null) return null;

        return ResponseDTOs.AppealResponseDTO.builder()
                .id(appeal.getId())
                .videoId(appeal.getVideo() != null ? appeal.getVideo().getId() : null)
                .reason(appeal.getReason())
                .processed(appeal.isProcessed())
                .build();
    }

    public static ResponseDTOs.MonetizationInfoResponseDTO toMonetizationInfoDTO(MonetizationInfo info) {
        if (info == null) return null;

        return ResponseDTOs.MonetizationInfoResponseDTO.builder()
                .videoId(info.getVideo() != null ? info.getVideo().getId() : null)
                .percent(info.getPercent())
                .isAgreed(info.getIsAgreed())
                .build();
    }

    public static List<ResponseDTOs.CommentResponseDTO> toCommentDTOList(List<Comment> comments) {
        if (comments == null) return null;

        return comments.stream()
                .map(DTOMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    public static List<ResponseDTOs.VideoInfoResponseDTO> toVideoInfoDTOList(List<VideoInfo> videos) {
        if (videos == null) return null;

        return videos.stream()
                .map(DTOMapper::toVideoInfoDTO)
                .collect(Collectors.toList());
    }
}