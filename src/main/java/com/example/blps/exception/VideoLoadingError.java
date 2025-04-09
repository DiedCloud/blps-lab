package com.example.blps.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpServerErrorException;

public class VideoLoadingError extends HttpServerErrorException {
    public VideoLoadingError(String message) {
        super(HttpStatusCode.valueOf(503), message);
    }
}
