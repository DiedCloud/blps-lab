package com.example.blps.dao.delegate.comment;

import com.example.blps.service.AuthService;
import com.example.blps.service.CommentService;
import com.example.blps.service.UserService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateComment implements JavaDelegate {
    private final CommentService commentService;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String userId = delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId();

        try {
            Long videoId = Long.valueOf(String.valueOf(delegateExecution.getVariable("videoId")));
            String text = String.valueOf(delegateExecution.getVariable("text"));

            authService.checkTokenInDelegatedAuth(Long.valueOf(userId));

            var res = commentService.createComment(userService.getById(Long.valueOf(userId)), videoId, text);

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}
