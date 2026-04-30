package com.lms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────
// API Response Wrapper — used by all controllers
// ─────────────────────────────────────────────────
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
