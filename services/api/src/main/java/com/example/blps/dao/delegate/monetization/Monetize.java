package com.example.blps.dao.delegate.monetization;

import com.example.blps.service.AuthService;
import com.example.blps.service.UserService;
import com.example.blps.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Monetize implements JavaDelegate {
    private final VideoService videoService;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String userId = delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId();

        try {
            Long videoId = Long.valueOf(delegateExecution.getVariable("videoId"));

            if (!authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "request_monetization_on_any_video")) {
                throw new IllegalAccessException("Forbidden");
            }

            var res = videoService.requestMonetization(videoId, userService.getById(Long.valueOf(userId)));

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}