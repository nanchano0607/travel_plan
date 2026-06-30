package com.min.edu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 계획을 찾을 수 없습니다."),
    PLAN_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."),
    PLAN_ITEM_DAY_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "일정 일차는 여행 기간을 초과할 수 없습니다."),
    AI_TRIP_DAYS_EXCEEDED(HttpStatus.BAD_REQUEST, "AI 여행 일정은 최대 7일까지 생성할 수 있습니다."),
    AI_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 호출에 실패했습니다."),
    AI_RESPONSE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "AI 응답 형식이 올바르지 않습니다."),
    AI_RESPONSE_SCHEMA_INVALID(HttpStatus.BAD_REQUEST, "AI 응답 데이터 구조가 올바르지 않습니다."),
    PLACE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "장소 검증에 실패했습니다."),
    ;
    private final HttpStatus status;
    private final String message;
    public String getCode() {
        return name();
    }
}
