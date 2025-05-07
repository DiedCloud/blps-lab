package com.example.blps.dao.controller;

import com.example.blps.dao.controller.mapper.DTOMapper;
import com.example.blps.dao.controller.model.AppealRequestDTO;
import com.example.blps.dao.controller.model.ResponseDTOs;
import com.example.blps.entity.User;
import com.example.blps.service.AppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
@Tag(name = "Appeals", description = "Operations for handling video appeals")
public class AppealController {
    private final AppealService appealService;

    @PostMapping
    @Operation(summary = "Submit an appeal for a rejected video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appeal submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Appeal already submitted"),
            @ApiResponse(responseCode = "403", description = "Access denied - not the author"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ResponseDTOs.ApiResponse<ResponseDTOs.AppealResponseDTO>> submit(
            @Valid @RequestBody AppealRequestDTO req,
            @AuthenticationPrincipal User principal) {

        var appeal = appealService.submitAppeal(req.videoId(), req.reason(), principal);
        ResponseDTOs.AppealResponseDTO appealDTO = DTOMapper.toAppealDTO(appeal);

        return ResponseEntity.ok(
                ResponseDTOs.ApiResponse.success(appealDTO, "Appeal submitted successfully")
        );
    }
}