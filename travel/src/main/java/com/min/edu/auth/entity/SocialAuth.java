package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "social_auth",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        }
)
/*
    provider 와 provider_user_id가 겹치면 안되기 때문에
    ex) user_id : 1 , provider : google , provider_user_id : 1234
        user_id : 2 , provider : google , provider_user_id : 1234
        이러면 원하는 사용자를 찾을수가 없음
 */

public class SocialAuth {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "provider_email", nullable = false)
    private String providerEmail;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;
}
