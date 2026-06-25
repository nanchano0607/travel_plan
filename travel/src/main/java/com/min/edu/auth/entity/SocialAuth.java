package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "soical_auth",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "proder_user_id"})
        }
)
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
