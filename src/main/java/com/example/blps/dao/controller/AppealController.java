package com.example.blps.dao.controller;

import com.example.blps.dao.controller.model.AppealRequestDTO;
import com.example.blps.dao.repository.model.Appeal;
import com.example.blps.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.blps.service.AppealService;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
public class AppealController {
    private final AppealService appealService;

    @PostMapping
    public Long submit(@RequestBody AppealRequestDTO req,
                         @AuthenticationPrincipal User principal) {
        return appealService.submitAppeal(req.videoId(), req.reason(), principal).getId();
    }
}

