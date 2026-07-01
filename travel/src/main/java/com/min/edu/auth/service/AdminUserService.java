package com.min.edu.auth.service;

import com.min.edu.auth.dto.UpdateProfileResponse;
import com.min.edu.auth.dto.UpdateRoleRequest;
import com.min.edu.auth.dto.UpdateRoleResponse;
import com.min.edu.auth.entity.Role;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;

    public UpdateRoleResponse updateUserRole(Long userId, Role role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.changeRole(role);

        return UpdateRoleResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
