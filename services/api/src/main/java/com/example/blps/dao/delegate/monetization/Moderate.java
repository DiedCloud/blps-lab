package com.example.blps.dao.delegate.monetization;

import com.example.blps.service.*;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Named("moderate")
@RequiredArgsConstructor
public class Moderate implements JavaDelegate {
    private final VideoService videoService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long videoId = Long.valueOf(String.valueOf(delegateExecution.getVariable("videoId")));
            boolean approved = Boolean.parseBoolean(String.valueOf(delegateExecution.getVariable("approved")));

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