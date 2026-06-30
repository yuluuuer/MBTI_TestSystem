package com.mbti.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean ok;
    private String message;

    public static ApiResponse success() {
        return ApiResponse.builder().ok(true).build();
    }

    public static ApiResponse success(String message) {
        return ApiResponse.builder().ok(true).message(message).build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder().ok(false).message(message).build();
    }
}
