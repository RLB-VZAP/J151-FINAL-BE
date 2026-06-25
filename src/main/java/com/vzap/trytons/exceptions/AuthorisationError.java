package com.vzap.trytons.exceptions;

public class AuthorisationError extends ApplicationException {
    public AuthorisationError(String message) {
        super(message,422,"FORBIDDEN");
    }
    public AuthorisationError(String message, Throwable cause) {
        super(message,422,"FORBIDDEN",cause);
    }
}
