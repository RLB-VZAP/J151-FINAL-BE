package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    public static <T> AuthApiResponse<T> success(String message, T data) {
        return new AuthApiResponse<>(true, message, data);
    }
}