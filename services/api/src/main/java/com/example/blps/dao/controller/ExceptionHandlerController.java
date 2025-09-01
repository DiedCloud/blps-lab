package com.example.blps.dao.controller;

import com.example.blps.exception.IllegalContent;
import com.example.blps.exception.VideoLoadingError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerController {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
        private String timestamp;

        public static ErrorResponse of(String message, int status) {
            return new ErrorResponse(
                    message,
                    status,
                    LocalDateTime.now().toString()
            );
        }
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> authException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noSuchElementException(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalContent.class)
    public ResponseEntity<ErrorResponse> illegalContent(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErrorResponse> tooManyRequests(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ExceptionHandler(VideoLoadingError.class)
    public ResponseEntity<ErrorResponse> videoLoadingError(HttpServerErrorException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorResponse.of(ex.getMessage(), ex.getStatusCode().value()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("Validation failed: " + errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generalException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}