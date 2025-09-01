package com.example.blps.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpServerErrorException;

public class TranscriptionException extends HttpServerErrorException {
    public TranscriptionException(String message, Exception e) {
        super(HttpStatusCode.valueOf(503), "%s: %s".formatted(message, e.getMessage()));
    }

    public TranscriptionException(String message) {
        super(HttpStatusCode.valueOf(503), message);
    }
}
