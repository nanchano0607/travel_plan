package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // 신규 가입자는 이메일 인증 전까지 PENDING 유지(REQ-AUTH-06).
    // DB 컬럼 기본값은 기존 계정 보존을 위해 ACTIVE로 다르게 설정됨(V11 마이그레이션 참고)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LocalAuthStatus status = LocalAuthStatus.PENDING;
}
