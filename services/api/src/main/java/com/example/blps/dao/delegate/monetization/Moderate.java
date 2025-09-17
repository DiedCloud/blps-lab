package com.example.blps.dao.delegate.monetization;

import com.example.blps.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Moderate implements JavaDelegate {
    private final VideoService videoService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long videoId = Long.valueOf(delegateExecution.getVariable("videoId"));
            Boolean approved = Boolean.valueOf(delegateExecution.getVariable("approved"));

            if (authService.hasNoAuthorityInDelegatedAuth(delegateExecution, "ROLE_MODERATOR", "ROLE_ADMIN", "moderate_monetization_request")) {
                throw new IllegalAccessException("Forbidden");
            }

            var res = videoService.moderate(videoId, approved);

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}