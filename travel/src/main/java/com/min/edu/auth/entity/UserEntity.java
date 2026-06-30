package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "user_auth")
@Getter
@EqualsAndHashCode(of = "userId")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // local, google, kakao, naver 최초 가입 경로
    @Column(length = 50)
    private String provider;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String nickname;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "user_image")
    private Long userImage;

    @Builder
    private UserEntity(String email, String provider, String name, String nickname,
                        String phone, Role role, Long userImage) {
        this.email = email;
        this.provider = provider;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role != null ? role : Role.USER;
        this.userImage = userImage;
    }

    public void updateProfile(String name, String nickname, String phone) {
        if (name != null) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }
}
