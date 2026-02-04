package com.batuhan.banking_service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> GlobalResponse<T> success(T data, String message) {
        return GlobalResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> GlobalResponse<T> error(String message) {
        return GlobalResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> GlobalResponse<T> error(String message, T data) {
        return GlobalResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}