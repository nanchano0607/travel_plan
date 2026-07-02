package com.min.edu.auth.service;

import com.min.edu.auth.dto.UpdateProfileResponse;
import com.min.edu.auth.dto.UserProfileResponse;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 내 정보 조회 (본인만)
    @Transactional(readOnly = true)
    public UpdateProfileResponse getMyInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UpdateProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .build();
    }

    // 공개 프로필 조회 (누구나)
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.fromEntity(user);
    }

}
