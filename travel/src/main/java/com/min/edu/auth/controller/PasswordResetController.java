package com.min.edu.auth.controller;

import com.min.edu.auth.dto.PasswordResetConfirmRequest;
import com.min.edu.auth.dto.PasswordResetRequest;
import com.min.edu.auth.service.PasswordResetService;
import com.min.edu.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 비밀번호 재설정 API
 *
 * @author 이유진
 * @version 1.0
 */
@Tag(name = "Auth", description = "회원가입 / 로그인 API")
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 요청", description = "이메일로 재설정 링크를 발송합니다.")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> request(
            @Valid @RequestBody PasswordResetRequest dto
    ) {
        passwordResetService.requestReset(dto.email());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 링크가 이메일로 발송되었습니다.", null));
    }

    @Operation(summary = "비밀번호 재설정 확인", description = "토큰을 검증하고 새 비밀번호로 변경합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @Valid @RequestBody PasswordResetConfirmRequest dto
    ) {
        passwordResetService.confirmReset(dto.token(), dto.newPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }
}
