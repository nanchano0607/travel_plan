package com.min.edu.auth.controller;

import com.min.edu.auth.dto.EmailVerificationConfirmRequest;
import com.min.edu.auth.dto.EmailVerificationRequest;
import com.min.edu.auth.service.EmailVerificationService;
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
 * 이메일 인증 코드 발송 및 확인 API를 제공하는 컨트롤러(REQ-AUTH-06)
 *
 * @author 이유진
 * @version 1.0
 */
@Tag(name = "Auth", description = "회원가입 / 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email-verifications")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증 코드 발송/재발송")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "재발송 횟수/간격 초과")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestVerification(
            @Valid @RequestBody EmailVerificationRequest request) {
        emailVerificationService.requestVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>success(null));
    }

    @Operation(summary = "이메일 인증 코드 확인")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "코드 불일치/이미 사용됨")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "코드 만료")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerification(
            @Valid @RequestBody EmailVerificationConfirmRequest request) {
        emailVerificationService.confirmVerification(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.<Void>success(null));
    }
}
