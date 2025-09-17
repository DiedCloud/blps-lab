package com.example.blps.dao.delegate.monetization;

import com.example.blps.service.AppealService;
import com.example.blps.service.AuthService;
import com.example.blps.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmitAppeal implements JavaDelegate {
    private final AppealService appealService;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String userId = delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId();

        try {
            Long videoId = Long.valueOf(delegateExecution.getVariable("videoId"));
            String reason = String.valueOf(delegateExecution.getVariable("reason"));

            if (!authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "appeal_monetization_on_any_video")) {
                throw new IllegalAccessException("Forbidden");
            }

            var res = appealService.submitAppeal(videoId, reason, userService.getById(Long.valueOf(userId)));

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}