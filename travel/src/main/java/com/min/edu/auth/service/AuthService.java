package com.min.edu.auth.service;

import com.min.edu.auth.dto.LoginRequest;
import com.min.edu.auth.dto.LoginResponse;
import com.min.edu.auth.dto.SignupRequest;
import com.min.edu.auth.dto.SignupResponse;
import com.min.edu.auth.dto.UpdateProfileRequest;
import com.min.edu.auth.dto.UpdateProfileResponse;
import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.auth.entity.EmailVerification;
import com.min.edu.auth.entity.LocalAuth;
import com.min.edu.auth.entity.LocalAuthStatus;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.security.jwt.JwtTokenProvider;
import com.min.edu.auth.repository.EmailVerificationRepository;
import com.min.edu.auth.repository.LocalAuthRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final LocalAuthRepository localAuthRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 이메일 입력 단계에서 인증이 완료된 이메일만 가입을 허용(이메일 인증 선행 정책)
        EmailVerification verification = emailVerificationRepository.findById(request.getEmail())
                .filter(EmailVerification::isVerified)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_VERIFIED));

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .provider(AuthProvider.LOCAL)
                .name(request.getName())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        LocalAuth localAuth = new LocalAuth();
        localAuth.setUserId(user.getUserId());
        localAuth.setPassword(passwordEncoder.encode(request.getPassword()));
        localAuth.setStatus(LocalAuthStatus.ACTIVE); // 가입 시점에 이미 이메일 인증이 끝난 상태
        localAuthRepository.save(localAuth);

        // 더 이상 필요 없는 인증 기록 정리
        emailVerificationRepository.delete(verification);

        return SignupResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }

    @Transactional(noRollbackFor = CustomException.class)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        LocalAuth localAuth = localAuthRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        if (localAuth.isLoginLocked()) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (localAuth.isLoginLockExpired()) {
            localAuth.resetLoginFailure();
        }

        if (!passwordEncoder.matches(request.getPassword(), localAuth.getPassword())) {
            localAuth.increaseFailedLoginCount();
            if (localAuth.getFailedLoginCount() >= 5) {
                localAuth.lockLogin();
                throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
            }
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        log.info("passwordEncoder matching pass");
        // REQ-AUTH-06: 이메일 인증 전(PENDING) 계정은 로그인 차단
        if (localAuth.getStatus() != LocalAuthStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        localAuth.resetLoginFailure();

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
        log.info("accessToken providing pass");
        System.out.println("Hello");

        return LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .accessToken(accessToken)
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

    // 내 정보 조회 API 2026.07.02 이희경 추가
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

}