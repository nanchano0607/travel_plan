package com.min.edu.auth.controller;

import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.auth.entity.SocialAuth;
import com.min.edu.auth.oauth.SocialLinkService;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Social Link", description = "소셜 계정 연결/해제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/social-link")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    // 연결 시작: 세션에 userId 저장 후 OAuth2 URL 반환 → 클라이언트가 해당 URL로 리다이렉트
    @Operation(
            summary = "소셜 계정 연결 시작",
            description = "반환된 URL로 리다이렉트하면 소셜 로그인 후 현재 계정에 연결됩니다."
    )
    @PostMapping("/start/{provider}")
    public ResponseEntity<ApiResponse<String>> startLink(
            Authentication authentication,
            @PathVariable String provider,
            HttpSession session
    ) {
        Long userId = (Long) authentication.getPrincipal();

        AuthProvider authProvider;
        try {
            authProvider = AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }
        if (authProvider == AuthProvider.LOCAL) {
            throw new CustomException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }

        session.setAttribute("SOCIAL_LINK_USER_ID", userId);
        session.setAttribute("SOCIAL_LINK_PROVIDER", authProvider.name());

        String oauthUrl = "/oauth2/authorization/" + provider.toLowerCase();

        return ResponseEntity.ok(ApiResponse.success(oauthUrl));
    }

    @Operation(summary = "연결된 소셜 계정 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SocialAuth>>> getLinkedSocials(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<SocialAuth> socials = socialLinkService.getLinkedSocials(userId);

        return ResponseEntity.ok(ApiResponse.success(socials));
    }

    @Operation(summary = "소셜 계정 해제")
    @DeleteMapping("/{provider}")
    public ResponseEntity<ApiResponse<Void>> unlinkSocial(
            Authentication authentication,
            @PathVariable String provider
    ) {
        Long userId = (Long) authentication.getPrincipal();

        AuthProvider authProvider;
        try {
            authProvider = AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        }

        socialLinkService.unlink(userId, authProvider);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
