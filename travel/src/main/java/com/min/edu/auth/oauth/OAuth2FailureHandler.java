package com.min.edu.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String errorCode = classifyError(exception);

        // 민감정보(스택 추적, 인가 코드, client secret)는 응답에 포함하지 않음
        response.sendRedirect(FRONTEND_URL + "/auth?error=" + errorCode);
    }

    // 실패 원인을 서비스 오류 코드로 매핑
    private String classifyError(AuthenticationException exception) {
        if (!(exception instanceof OAuth2AuthenticationException oAuthEx)) {
            return "OAUTH_CANCELLED";
        }

        String code = oAuthEx.getError().getErrorCode();

        return switch (code) {
            // 동일 이메일로 이미 가입된 계정 존재 (REQ-AUTH-12)
            case "email_conflict" -> "SOCIAL_EMAIL_CONFLICT";
            // 소셜 계정이 이미 다른 계정에 연결됨 (REQ-AUTH-12)
            case "social_already_linked" -> "SOCIAL_ALREADY_LINKED";
            // 사용자가 소셜 로그인 창에서 취소
            case "access_denied" -> "OAUTH_CANCELLED";
            // state 불일치 또는 인가 요청 세션 없음
            case "invalid_state", "authorization_request_not_found" -> "OAUTH_STATE_MISMATCH";
            // 인가 코드 교환 실패
            case "invalid_token_response", "invalid_id_token" -> "OAUTH_CODE_EXCHANGE_FAILED";
            // 공급자 서버 장애
            case "server_error", "temporarily_unavailable" -> "OAUTH_PROVIDER_ERROR";
            default -> "OAUTH_CANCELLED";
        };
    }
}
