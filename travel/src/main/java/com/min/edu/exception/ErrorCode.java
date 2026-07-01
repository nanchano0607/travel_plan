package com.min.edu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    // REQ-AUTH-06 이메일 인증: 만료는 410(GONE)으로 구분하고, 재발송 제한은 표준 rate-limit 규약인 429로 응답
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않은 계정입니다."),
    // 이메일 인증이 회원가입보다 선행되어야 함(인증 미완료 상태로 가입 제출 시)
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증을 먼저 완료해주세요."),
    VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "발급된 인증 코드가 없습니다. 인증을 다시 요청해주세요."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    VERIFICATION_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 인증 코드입니다."),
    VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "인증 시도 횟수를 초과했습니다. 인증을 다시 요청해주세요."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "인증 코드가 만료되었습니다. 인증을 다시 요청해주세요."),
    VERIFICATION_TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "인증 코드 재발송 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_USER(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 계획을 찾을 수 없습니다."),
    PLAN_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "일정 항목을 찾을 수 없습니다."),
    PLAN_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."),
    PLAN_ITEM_DAY_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "일정 일차는 여행 기간을 초과할 수 없습니다."),
    PLAN_ITEM_ORDER_INVALID(HttpStatus.BAD_REQUEST, "일정 항목 순서가 올바르지 않습니다."),
    AI_TRIP_DAYS_EXCEEDED(HttpStatus.BAD_REQUEST, "AI 여행 일정은 최대 7일까지 생성할 수 있습니다."),
    AI_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 호출에 실패했습니다."),
    AI_REQUEST_IN_PROGRESS(HttpStatus.CONFLICT, "이미 AI 일정 생성 요청이 진행 중입니다."),
    AI_REQUEST_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘 AI 일정 생성 횟수를 초과했습니다."),
    AI_REQUEST_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요."),
    AI_RESPONSE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "AI 응답 형식이 올바르지 않습니다."),
    AI_RESPONSE_SCHEMA_INVALID(HttpStatus.BAD_REQUEST, "AI 응답 데이터 구조가 올바르지 않습니다."),
    PLACE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "장소 검증에 실패했습니다."),

    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 취소했습니다."),

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;
    public String getCode() {
        return name();
    }
}