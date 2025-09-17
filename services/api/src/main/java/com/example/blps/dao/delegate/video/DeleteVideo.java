package com.example.blps.dao.delegate.video;

import com.example.blps.service.AuthService;
import com.example.blps.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteVideo implements JavaDelegate {
    private final VideoService videoService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long videoId = Long.valueOf(String.valueOf(delegateExecution.getVariable("videoId")));

            if (!authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "delete_any_video")) {
                throw new IllegalAccessException("Forbidden");
            }

            videoService.deleteVideoById(videoId);

            delegateExecution.setVariable("result", "Video deleted");
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}