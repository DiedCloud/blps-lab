package com.example.blps.dao.delegate.video;

import com.example.blps.service.AuthService;
import com.example.blps.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EditVideo implements JavaDelegate {
    private final VideoService videoService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long videoId = Long.valueOf(delegateExecution.getVariable("videoId"));
            String title = String.valueOf(delegateExecution.getVariable("title"));
            String description = String.valueOf(delegateExecution.getVariable("description"));

            if (!authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "edit_any_video")) {
                throw new IllegalAccessException("Forbidden");
            }

            var res = videoService.editVideoInfo(videoId, description, title);

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}