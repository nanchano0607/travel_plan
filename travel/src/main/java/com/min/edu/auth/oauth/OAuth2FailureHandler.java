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

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        OAuthErrorResult result = classifyError(exception);

        response.setStatus(result.httpStatus());
        response.setContentType("application/json;charset=UTF-8");

        // 민감정보(스택 추적, 인가 코드, client secret)는 응답에 포함하지 않음
        response.getWriter().write("{\"errorCode\": \"" + result.errorCode() + "\"}");
    }

    // 실패 원인을 서비스 오류 코드로 매핑
    private OAuthErrorResult classifyError(AuthenticationException exception) {
        if (!(exception instanceof OAuth2AuthenticationException oAuthEx)) {

            return new OAuthErrorResult(HttpServletResponse.SC_UNAUTHORIZED, "OAUTH_CANCELLED");
        }

        String code = oAuthEx.getError().getErrorCode();

        return switch (code) {
            // 동일 이메일로 이미 가입된 계정 존재 (REQ-AUTH-12)
            case "email_conflict" ->
                    new OAuthErrorResult(HttpServletResponse.SC_CONFLICT, "SOCIAL_EMAIL_CONFLICT");
            // 소셜 계정이 이미 다른 계정에 연결됨 (REQ-AUTH-12)
            case "social_already_linked" ->
                    new OAuthErrorResult(HttpServletResponse.SC_CONFLICT, "SOCIAL_ALREADY_LINKED");
            // 사용자가 소셜 로그인 창에서 취소
            case "access_denied" ->
                    new OAuthErrorResult(HttpServletResponse.SC_BAD_REQUEST, "OAUTH_CANCELLED");
            // state 불일치 또는 인가 요청 세션 없음
            case "invalid_state", "authorization_request_not_found" ->
                    new OAuthErrorResult(HttpServletResponse.SC_BAD_REQUEST, "OAUTH_STATE_MISMATCH");
            // 인가 코드 교환 실패
            case "invalid_token_response", "invalid_id_token" ->
                    new OAuthErrorResult(HttpServletResponse.SC_BAD_GATEWAY, "OAUTH_CODE_EXCHANGE_FAILED");
            // 공급자 서버 장애
            case "server_error", "temporarily_unavailable" ->
                    new OAuthErrorResult(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "OAUTH_PROVIDER_ERROR");
            default ->
                    new OAuthErrorResult(HttpServletResponse.SC_UNAUTHORIZED, "OAUTH_CANCELLED");
        };
    }

    private record OAuthErrorResult(int httpStatus, String errorCode) {}
}
