package com.min.edu.auth.oauth;

import com.min.edu.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (oAuth2User.isLinkMode()) {
            handleLinkSuccess(request, response);
            return;
        }

        handleLoginSuccess(response, oAuth2User);
    }

    // 소셜 계정 연결 성공: 세션 정리 후 프론트로 리다이렉트
    private void handleLinkSuccess(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("SOCIAL_LINK_USER_ID");
            session.removeAttribute("SOCIAL_LINK_PROVIDER");
        }

        response.sendRedirect("http://localhost:5173/?linked=true");
    }

    // 소셜 로그인 성공: 자체 JWT를 쿼리 파라미터로 프론트에 전달 (REQ-AUTH-09)
    private void handleLoginSuccess(
            HttpServletResponse response,
            CustomOAuth2User oAuth2User
    ) throws IOException {
        String accessToken = jwtTokenProvider.createAccessToken(
                oAuth2User.getUserId(),
                oAuth2User.getEmail(),
                oAuth2User.getRole()
        );

        response.sendRedirect("http://localhost:5173/?token=" + accessToken);
    }
}
