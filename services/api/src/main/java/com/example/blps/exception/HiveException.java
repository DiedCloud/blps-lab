package com.example.blps.exception;

public class HiveException extends RuntimeException {

    public HiveException(String message) {
        super(message);
    }

    public HiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
