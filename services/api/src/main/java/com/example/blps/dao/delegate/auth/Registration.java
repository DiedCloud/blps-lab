package com.example.blps.dao.delegate.auth;

import com.example.blps.service.AuthService;
import com.example.blps.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Registration implements JavaDelegate {
    private final AuthService authService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Long userId = Long.valueOf(
                delegateExecution.getProcessEngineServices().getIdentityService().getCurrentAuthentication().getUserId()
        );
        try {
            String login = String.valueOf(delegateExecution.getVariable("login"));
            String password = String.valueOf(delegateExecution.getVariable("password"));
            String name = String.valueOf(delegateExecution.getVariable("name"));
            String token = authService.register(login, password, name);
            delegateExecution.setVariable("result", token);
            TokenService.putUserToken(userId, token);
        } catch (Throwable throwable) {
            TokenService.putUserToken(userId, null);
            delegateExecution.setVariable("error", throwable.getMessage());
            throw new BpmnError("register_error", throwable.getMessage());
        }
    }
}