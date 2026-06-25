package com.min.edu.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
}