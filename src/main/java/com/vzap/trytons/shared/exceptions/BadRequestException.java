package com.vzap.trytons.shared.exceptions;

public class BadRequestException extends ApplicationException {
    public BadRequestException(String message) {
        super(message, 409, "BAD_REQUEST");
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, 409, "BAD_REQUEST", cause);
    }
}
