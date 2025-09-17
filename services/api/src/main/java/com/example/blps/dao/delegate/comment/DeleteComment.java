package com.example.blps.dao.delegate.comment;

import com.example.blps.service.AuthService;
import com.example.blps.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteComment implements JavaDelegate {
    private final CommentService commentService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long commentId = Long.valueOf(String.valueOf(delegateExecution.getVariable("commentId")));
            Long videoId = Long.valueOf(String.valueOf(delegateExecution.getVariable("videoId")));

            if (
                    !authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "delete_any_comment") &&
                    !authService.hasPermissionInDelegatedAuth(delegateExecution, commentId, "Comment", "delete_any_comment")
            ) {
                throw new IllegalAccessException("Forbidden");
            }

            commentService.dropComment(commentId);

            delegateExecution.setVariable("result", "Comment deleted");
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}
