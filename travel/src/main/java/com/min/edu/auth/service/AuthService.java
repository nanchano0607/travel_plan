package com.min.edu.auth.service;

import com.min.edu.auth.dto.SignupRequest;
import com.min.edu.auth.dto.SignupResponse;
import com.min.edu.auth.entity.LocalAuth;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.repository.LocalAuthRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}