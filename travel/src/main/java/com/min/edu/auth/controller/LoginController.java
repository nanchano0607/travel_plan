package com.min.edu.auth.controller;

import com.min.edu.auth.dto.LoginRequest;
import com.min.edu.auth.dto.LoginResponse;
import com.min.edu.auth.service.AuthService;
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

@Tag(name = "Auth", description = "회원가입 / 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthService authService;

    /**
     * 로컬 로그인 처리 API
     *
     * @author 이유진
     * @version 1.0
     */
    @Operation(summary = "로그인", description = "이메일, 비밀번호 기반 로컬 로그인을 처리합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
