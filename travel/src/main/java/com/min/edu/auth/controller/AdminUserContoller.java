package com.min.edu.auth.controller;

import com.min.edu.auth.dto.UpdateRoleRequest;
import com.min.edu.auth.dto.UpdateRoleResponse;
import com.min.edu.auth.service.AdminUserService;
import com.min.edu.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 권한 부여 API
 *
 * @author 박진현
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserContoller {

    private final AdminUserService adminUserService;

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UpdateRoleResponse>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request
    ){
        UpdateRoleResponse response = adminUserService.updateUserRole(userId, request.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
