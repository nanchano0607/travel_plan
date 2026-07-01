package com.min.edu.auth.service;

import com.min.edu.auth.entity.LocalAuth;
import com.min.edu.auth.entity.PasswordResetToken;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.mail.VerificationMailSender;
import com.min.edu.auth.repository.LocalAuthRepository;
import com.min.edu.auth.repository.PasswordResetTokenRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    // 요구사항: 재설정 링크는 발급 후 30분간 유효
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final LocalAuthRepository localAuthRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    // enumeration 방지: 가입되지 않은 이메일이어도 동일하게 성공 응답 반환
    public void requestReset(String email) {
        LocalDateTime now = LocalDateTime.now();
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        UserEntity user = userOpt.get();

        // 소셜 전용 계정(local_auth 행 없음)은 재설정 불가 → 조용히 종료
        if (localAuthRepository.findById(user.getUserId()).isEmpty()) {
            return;
        }

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken token = PasswordResetToken.issue(
                user.getUserId(), tokenHash, now.plus(TOKEN_TTL), now);
        passwordResetTokenRepository.save(token);

        mailSender.sendPasswordResetLink(email, rawToken);
    }

    public void confirmReset(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        // 만료 > 사용됨 순으로 검증 (더 구체적인 사유를 우선 노출)
        if (token.isExpired(now)) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        if (token.isUsed()) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_ALREADY_USED);
        }

        LocalAuth localAuth = localAuthRepository.findById(token.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        localAuth.setPassword(passwordEncoder.encode(newPassword));
        token.markUsed(now);
    }

    // 32바이트 SecureRandom → Base64 URL-safe 인코딩 (URL 쿼리 파라미터로 안전하게 전달)
    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // 원문은 이메일로만 전달하고 DB에는 해시만 저장
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
