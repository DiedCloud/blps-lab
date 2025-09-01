package com.example.blps.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    @Bean("transcribeExecutor")
    public ThreadPoolTaskExecutor transcribeExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(5);
        exec.setMaxPoolSize(20);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("transcribe-");
        exec.initialize();
        return exec;
    }

    @Override
    public Executor getAsyncExecutor() {
        return transcribeExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            String msg = "Uncaught async error in " + method.getName();
            log.error(msg, ex);
        };
    }
}
