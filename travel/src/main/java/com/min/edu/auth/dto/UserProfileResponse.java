package com.min.edu.auth.dto;

import com.min.edu.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String nickname;
    private Long userImage; // 프로필 이미지 Id

    public static UserProfileResponse fromEntity(UserEntity user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .userImage(user.getUserImage())
                .build();
    }

}
