package com.batuhan.banking_service.dto.common;

public record GlobalResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> GlobalResponse<T> success(T data, String message) {
        return new GlobalResponse<>(true, message, data);
    }

    public static <T> GlobalResponse<T> error(String message) {
        return new GlobalResponse<>(false, message, null);
    }

    public static <T> GlobalResponse<T> error(String message, T data) {
        return new GlobalResponse<>(false, message, data);
    }
}