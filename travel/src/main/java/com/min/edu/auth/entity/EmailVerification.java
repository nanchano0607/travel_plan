package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 로컬 회원가입 "이전" 단계에서 이메일 인증 코드 발급/검증 상태를 저장하는 엔티티.
 * 회원가입 시점에는 아직 계정(user_auth)이 존재하지 않으므로 이메일을 PK로 사용한다.
 * 이메일당 1행만 유지하며, 재발송 시 기존 코드를 덮어써 폐기한다(REQ-AUTH-06).
 *
 * @author 이유진
 * @version 1.0
 */
@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "resend_count", nullable = false)
    private int resendCount;

    @Column(name = "window_started_at", nullable = false)
    private LocalDateTime windowStartedAt;

    public static EmailVerification issue(String email, String codeHash, LocalDateTime expiresAt, LocalDateTime now) {
        EmailVerification verification = new EmailVerification();

        verification.email = email;
        verification.codeHash = codeHash;
        verification.expiresAt = expiresAt;
        verification.lastSentAt = now;
        verification.windowStartedAt = now;
        verification.resendCount = 1;

        return verification;
    }

    // 계정이 생기기 전에 검증되므로 "사용됨"이 아니라 "인증 완료됨"의 의미로 사용
    public boolean isVerified() {
        return usedAt != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean matchesCode(String candidateHash) {
        return this.codeHash.equals(candidateHash);
    }

    public boolean isAttemptsExceeded(int maxAttempts) {
        return failCount >= maxAttempts;
    }

    public void markVerified(LocalDateTime now) {
        this.usedAt = now;
    }

    public void increaseFailCount() {
        this.failCount++;
    }

    public boolean isCooldownActive(LocalDateTime now, Duration cooldown) {
        return now.isBefore(lastSentAt.plus(cooldown));
    }

    public boolean isResendLimitReached(int maxResend) {
        return resendCount >= maxResend;
    }

    // 영구 잠금 방지: 윈도우가 지나면 재발송 횟수를 리셋해 다시 재발송할 수 있게 함
    public void resetWindowIfExpired(LocalDateTime now, Duration window) {
        if (now.isAfter(windowStartedAt.plus(window))) {
            this.resendCount = 0;
            this.windowStartedAt = now;
        }
    }

    public void reissue(String codeHash, LocalDateTime expiresAt, LocalDateTime now) {
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.usedAt = null;
        this.failCount = 0;
        this.lastSentAt = now;
        this.resendCount++;
    }
}
