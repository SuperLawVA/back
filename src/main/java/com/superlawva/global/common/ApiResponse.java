package com.superlawva.global.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code;

    // 성공 응답
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, "SUCCESS");
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, "SUCCESS");
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, message, null, code);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, "ERROR");
    }
}