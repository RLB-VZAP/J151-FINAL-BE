package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private final boolean success;
    private final String message;
    private final String errorCode;

    public static ErrorResponse of(String message, String errorCode) {
        return new ErrorResponse(false, message, errorCode);
    }

    public static ErrorResponse authRequired(){
        return new ErrorResponse(false, "You need to be logged in.","AUTH_REQUIRED");
    }

    public static ErrorResponse invalidAuthToken(){
        return new ErrorResponse(false, "Invalid or expired token.","INVALID_AUTH_TOKEN");
    }

    public static ErrorResponse adminAuth(){
        return new ErrorResponse(false, "Admin authorisation required", "ADMIN_REQUIRED");
    }

    public static ErrorResponse internalServerError(){
        return new ErrorResponse(false, "Unexpected error occurred", "INTERNAL_SERVER_ERROR");
    }
}