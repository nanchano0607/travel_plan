package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_auth")
@Getter
@Setter
@NoArgsConstructor
public class LocalAuth {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // 신규 가입자는 이메일 인증 전까지 PENDING 유지(REQ-AUTH-06).
    // DB 컬럼 기본값은 기존 계정 보존을 위해 ACTIVE로 다르게 설정됨(V11 마이그레이션 참고)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LocalAuthStatus status = LocalAuthStatus.PENDING;

    public void increaseFailedLoginCount() {
        this.failedLoginCount++;
    }

    public void lockLogin() {
        this.lockedUntil = LocalDateTime.now().plusMinutes(10);
    }

    public boolean isLoginLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isLoginLockExpired() {
        return lockedUntil != null && !lockedUntil.isAfter(LocalDateTime.now());
    }

    public void resetLoginFailure() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }
}
