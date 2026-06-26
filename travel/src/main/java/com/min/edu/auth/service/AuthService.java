package com.min.edu.auth.service;

import com.min.edu.auth.dto.LoginRequest;
import com.min.edu.auth.dto.LoginResponse;
import com.min.edu.auth.dto.SignupRequest;
import com.min.edu.auth.dto.SignupResponse;
import com.min.edu.auth.dto.UpdateProfileRequest;
import com.min.edu.auth.dto.UpdateProfileResponse;
import com.min.edu.auth.entity.LocalAuth;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.repository.LocalAuthRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final LocalAuthRepository localAuthRepository;
    private final PasswordEncoder passwordEncoder;
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .provider("local")
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .build();
        userRepository.save(user);
        LocalAuth localAuth = new LocalAuth();
        localAuth.setUserId(user.getUserId());
        localAuth.setPassword(passwordEncoder.encode(request.getPassword()));
        localAuthRepository.save(localAuth);
        return SignupResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        LocalAuth localAuth = localAuthRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), localAuth.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    public UpdateProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateProfile(request.getName(), request.getNickname(), request.getPhone());

        return UpdateProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .build();
    }
}