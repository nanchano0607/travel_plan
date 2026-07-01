package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@IdClass(SocialAuthId.class)
@Table(
        name = "social_auth",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        }
)
public class SocialAuth {

    @Id
    @Column(name = "user_id")
    private Long userId;

    // (user_id, provider) 복합 PK — 한 사용자가 여러 provider 연결 지원
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_email", nullable = false)
    private String providerEmail;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Builder
    private SocialAuth(
            Long userId,
            AuthProvider provider,
            String providerUserId,
            String providerEmail,
            LocalDateTime linkedAt
    ) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.linkedAt = linkedAt;
    }
}
