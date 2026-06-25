package com.min.edu.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 성공/실패 응답 형식을 통일하기 위한 공통 응답 클래스
 *
 * @author 김찬호
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "OK", message, data);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
