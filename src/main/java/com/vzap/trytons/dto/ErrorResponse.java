package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final String errorCode;

    public static ErrorResponse of(String message, String errorCode) {
        return new ErrorResponse(false, message, errorCode);
    }
}