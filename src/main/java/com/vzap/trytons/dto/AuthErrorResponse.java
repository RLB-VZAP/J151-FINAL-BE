package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthErrorResponse {

    private final boolean success;
    private final String message;
    private final String errorCode;

    public static AuthErrorResponse of(String message, String errorCode) {
        return new AuthErrorResponse(false, message, errorCode);
    }
}