package com.vgerbot.propify.common;

public class PropifyException extends RuntimeException {
    public PropifyException(String message) {
        super(message);
    }

    public PropifyException(String message, Throwable cause) {
        super(message, cause);
    }
} 