package com.min.edu.auth.dto;

import com.min.edu.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private Role role;

    private String accessToken; // AccessToken 발급용 DTO

}
