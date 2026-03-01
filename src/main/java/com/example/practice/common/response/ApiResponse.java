package com.example.practice.common.response;

import com.example.practice.common.error.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 📦 모든 API 공통 응답 형식
 * {
 *   "result": "success" | "error",
 *   "data": {...},     // 성공시 데이터
 *   "message": "..."   // 에러 메시지
 * }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String result;
    private T data;
    private String message;

    // ✅ 성공 응답 (data만)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    // ✅ 성공 응답 (data + message)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }

    // ✅ 에러 응답
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }

    // ✅ AppException에서 사용
    public static <T> ApiResponse<T> error(AppException e) {
        return new ApiResponse<>("error", null, e.getMessage());
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("success", null, null);
    }
}
