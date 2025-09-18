package com.example.blps.dao.delegate.transcription;

import com.example.blps.service.AuthService;
import com.example.blps.service.TranscriptionService;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Component
@Named("getTranscription")
@RequiredArgsConstructor
public class GetTranscription implements JavaDelegate {
    private final TranscriptionService transcriptionService;
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            Long videoId = Long.valueOf(String.valueOf(delegateExecution.getVariable("videoId")));

            if (!authService.hasPermissionInDelegatedAuth(delegateExecution, videoId, "VideoInfo", "edit_any_video")) {
                throw new IllegalAccessException("Forbidden");
            }

            var res = transcriptionService.getTranscriptionByVideoId(videoId);

            delegateExecution.setVariable("result", res);
        } catch (Throwable throwable) {
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("error", throwable.getMessage());
        }
    }
}