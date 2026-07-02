package com.min.edu.auth.controller;

import com.min.edu.auth.dto.UpdateProfileResponse;
import com.min.edu.auth.dto.UserProfileResponse;
import com.min.edu.auth.service.UserService;
import com.min.edu.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userInfo")
public class UserController {

    private final UserService userService;

    // 내 정보 조회 API (본인만)
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UpdateProfileResponse> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return  ApiResponse.success(userService.getMyInfo(userId));
    }

    @Operation(summary = "공개 프로필 조회", description = "특정 사용자의 공개 프로필을 조회합니다.")
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserProfile(userId));
    }

}
