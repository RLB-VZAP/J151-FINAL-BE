package com.vzap.trytons.exceptions;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;


    public ApplicationException(String Message,int statusCode, String errorCode) {
        super(Message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    public ApplicationException(String Message,int statusCode, String errorCode, Throwable cause) {
        super(Message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}
