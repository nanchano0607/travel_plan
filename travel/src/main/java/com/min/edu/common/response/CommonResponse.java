package com.min.edu.common.response;

import com.min.edu.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CommonResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private CommonResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "OK", "요청이 성공했습니다.", data);
    }
    public static CommonResponse<Void> fail(ErrorCode errorCode) {
        return new CommonResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }
    public static CommonResponse<Void> fail(String code, String message) {
        return new CommonResponse<>(false, code, message, null);
    }
}