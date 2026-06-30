package com.min.edu.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateProfileResponse {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String phone;
}
