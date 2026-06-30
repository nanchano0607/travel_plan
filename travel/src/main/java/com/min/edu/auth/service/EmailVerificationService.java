package com.min.edu.auth.service;

import com.min.edu.auth.entity.EmailVerification;
import com.min.edu.auth.mail.VerificationMailSender;
import com.min.edu.auth.repository.EmailVerificationRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 회원가입 "이전" 단계에서 이메일 인증 코드 발급 및 검증을 처리하는 서비스 클래스(REQ-AUTH-06).
 * 이 시점에는 아직 계정이 존재하지 않으므로 이메일 자체를 기준으로 동작하며,
 * 인증 완료 여부는 회원가입 시점(AuthService.signup)에서 최종 확인된다.
 *
 * @author 이유진
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    // 요구사항: 인증 코드는 발급 후 5분 동안만 유효
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    // 재발송 어뷰징 방지를 위한 최소 재요청 간격
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    // 영구 잠금 방지를 위해 재발송 횟수를 일정 주기로 리셋
    private static final Duration RESEND_WINDOW = Duration.ofHours(1);
    private static final int MAX_RESEND_PER_WINDOW = 5;
    // 무차별 대입 방지: 일정 횟수 이상 틀리면 코드를 무효화하고 재발급을 강제
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final VerificationMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public void requestVerification(String email) {
        // 이미 가입된 이메일이면 인증이 무의미함(가입 단계에서 DUPLICATE_EMAIL로 막힘)
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        LocalDateTime now = LocalDateTime.now();
        String code = generateCode();
        String codeHash = hash(code);

        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElse(null);

        if (verification == null) {
            verification = EmailVerification.issue(email, codeHash, now.plus(CODE_TTL), now);
            emailVerificationRepository.save(verification);
        } else {
            if (verification.isCooldownActive(now, RESEND_COOLDOWN)) {
                throw new CustomException(ErrorCode.VERIFICATION_TOO_MANY_REQUESTS);
            }
            verification.resetWindowIfExpired(now, RESEND_WINDOW);
            if (verification.isResendLimitReached(MAX_RESEND_PER_WINDOW)) {
                throw new CustomException(ErrorCode.VERIFICATION_TOO_MANY_REQUESTS);
            }
            // 재요청 시 이전 코드는 폐기: 같은 행의 코드를 덮어써서 이전 코드는 더 이상 일치하지 않게 됨
            verification.reissue(codeHash, now.plus(CODE_TTL), now);
        }

        mailSender.sendVerificationCode(email, code);
    }

    public void confirmVerification(String email, String code) {
        // 이미 가입된 이메일이면 재확인할 필요가 없음
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // 인증완료됨 > 만료 > 시도횟수초과 > 불일치 순으로 더 구체적인 사유를 먼저 알려줌
        if (verification.isVerified()) {
            throw new CustomException(ErrorCode.VERIFICATION_ALREADY_USED);
        }
        if (verification.isExpired(now)) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (verification.isAttemptsExceeded(MAX_VERIFY_ATTEMPTS)) {
            throw new CustomException(ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED);
        }
        if (!verification.matchesCode(hash(code))) {
            verification.increaseFailCount();
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 계정이 아직 없으므로 여기서는 이메일 인증 완료만 기록.
        // 실제 계정 생성/활성화는 회원가입 제출 시점(AuthService.signup)에서 처리됨
        verification.markVerified(now);
    }

    private String generateCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    // 원문 코드는 저장하지 않고 해시만 저장(DB 유출 시에도 코드가 노출되지 않도록)
    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
